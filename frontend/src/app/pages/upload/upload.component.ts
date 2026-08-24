import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { HeaderComponent } from '../../shared/header/header.component';
import { FileApiService } from '../../core/service/file-api.service';
import { StoredFileUploadResponse } from '../../core/models/stored-file-upload-response.model';
import { AuthService } from '../../core/service/auth.service';

const MAX_FILE_SIZE_BYTES = 1024 * 1024 * 1024; // 1 Go
const FORBIDDEN_EXTENSIONS = ['exe', 'bat', 'cmd', 'com', 'msi', 'ps1', 'vbs', 'scr'];

interface ExpirationOption {
  value: number;
  label: string;
}

const EXPIRATION_OPTIONS: ExpirationOption[] = [
  { value: 1, label: 'Une journée' },
  { value: 2, label: '2 jours' },
  { value: 3, label: '3 jours' },
  { value: 4, label: '4 jours' },
  { value: 5, label: '5 jours' },
  { value: 6, label: '6 jours' },
  { value: 7, label: 'Une semaine' }
];

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HeaderComponent],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css'
})
export class UploadComponent {
  private formBuilder = inject(FormBuilder);
  private fileApiService = inject(FileApiService);
  private authService = inject(AuthService);

  readonly expirationOptions = EXPIRATION_OPTIONS;

  optionsForm: FormGroup = this.formBuilder.group({
    expirationDays: [7, [Validators.required, Validators.min(1), Validators.max(7)]],
    password: ['', [Validators.minLength(6)]]
  });

  selectedFile = signal<File | null>(null);
  uploading = signal(false);
  uploadResult = signal<StoredFileUploadResponse | null>(null);
  errorMessage = signal('');
  linkCopied = signal(false);
  submitted = false;

  fileTooLarge = computed(() => {
    const file = this.selectedFile();
    return file !== null && file.size > MAX_FILE_SIZE_BYTES;
  });

  fileExtensionForbidden = computed(() => {
    const file = this.selectedFile();
    if (!file) {
      return false;
    }
    const extension = this.extractExtension(file.name);
    return extension !== null && FORBIDDEN_EXTENSIONS.includes(extension.toLowerCase());
  });

  downloadLink = computed(() => {
    const result = this.uploadResult();
    if (!result) {
      return '';
    }
    return `${window.location.origin}/downloads/${result.downloadToken}`;
  });

  get form() {
    return this.optionsForm.controls;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.selectedFile.set(file);
    this.uploadResult.set(null);
    this.errorMessage.set('');
    this.submitted = false;

    input.value = '';
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage.set('');

    const file = this.selectedFile();

    if (!file || this.fileTooLarge() || this.fileExtensionForbidden() || this.optionsForm.invalid) {
      return;
    }

    this.uploading.set(true);

    const expirationDays = this.optionsForm.get('expirationDays')?.value;
    const password = this.optionsForm.get('password')?.value;

    this.fileApiService.uploadFile(file, expirationDays, password || null).subscribe({
      next: (response) => {
        this.uploading.set(false);
        this.uploadResult.set(response);
      },
      error: (error) => {
        this.uploading.set(false);
        if (error.status === 401) {
            this.authService.logout();
            this.errorMessage.set(
            'Votre session était invalide et a été réinitialisée. Vous pouvez réessayer votre envoi.'
            );
            return;
        }
        this.errorMessage.set(this.resolveErrorMessage(error));
      }
    });
  }

  copyLink(): Promise<void> {
    return navigator.clipboard.writeText(this.downloadLink()).then(() => {
      this.linkCopied.set(true);
      setTimeout(() => this.linkCopied.set(false), 2000);
    });
  }

  expirationPhrase(): string {
    const days = this.optionsForm.get('expirationDays')?.value ?? 7;
    if (days === 1) {
      return 'une journée';
    }
    if (days === 7) {
      return 'une semaine';
    }
    return `${days} jours`;
  }

  formatFileSize(bytes: number): string {
    const units = ['octets', 'Ko', 'Mo', 'Go'];
    let value = bytes;
    let unitIndex = 0;

    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex++;
    }

    const formatted = unitIndex === 0 ? String(value) : value.toFixed(1).replace('.', ',');
    return `${formatted} ${units[unitIndex]}`;
  }

  private extractExtension(fileName: string): string | null {
    const lastDot = fileName.lastIndexOf('.');
    if (lastDot < 0 || lastDot === fileName.length - 1) {
      return null;
    }
    return fileName.substring(lastDot + 1);
  }

  private resolveErrorMessage(error: any): string {
     if (error.status === 0) {
    return 'Impossible de contacter le serveur backend.';
    }
    if (error.error?.message) {
      return error.error.message;
    }
    switch (error.status) {
      case 400:
        return 'Requête invalide.';
      case 413:
        return 'Le fichier dépasse la taille maximale autorisée (1 Go).';
      case 415:
        return 'Type de requête non supporté.';
      default:
        return 'Serveur indisponible.';
    }
  }
}