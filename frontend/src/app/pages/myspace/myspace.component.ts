import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StoredFileList } from '../../core/models/stored-file-list.model';

import { HeaderComponent } from '../../shared/header/header.component';
import { FileApiService } from '../../core/service/file-api.service';
import { Router } from '@angular/router';
import { AuthService } from '../../core/service/auth.service';


@Component({
  selector: 'app-myspace',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './myspace.component.html',
  styleUrl: './myspace.component.css'
})
export class MySpaceComponent implements OnInit {
    private fileApiService = inject(FileApiService);
    private router = inject(Router);
    private authService = inject(AuthService);
    files = signal<StoredFileList[]>([]);
    filter = signal<'ALL' | 'VALID' | 'EXPIRED'>('ALL');
    loading = signal(false);
    errorMessage = signal('');
    successMessage = signal('');
    filteredFiles = computed(() => {
    const currentFilter = this.filter();

    if (currentFilter === 'ALL') {
     return this.files();
    }

        return this.files().filter(file => file.status === currentFilter);
    });


    ngOnInit(): void {
        this.loadFiles();
    }

    loadFiles(): void {
        this.loading.set(true);

        this.fileApiService.getFiles().subscribe({
            next: (files) => {
                this.files.set(files);
                this.loading.set(false);
            },
            error: (error) => {
                this.loading.set(false);
                if (error.status === 401) {
                    this.authService.logout();
                    this.router.navigate(['/login']);
                    return;
                }
                this.errorMessage.set('Impossible de charger les fichiers.');
            }
        });
    }

    deleteFile(file: StoredFileList): void {
        if (file.id === null) {
            return;
        }
        if (!confirm('Supprimer ce fichier?')) {
            return;
        }

        this.fileApiService.deleteFile(file.id).subscribe({
            next: () => {
                this.successMessage.set('Fichier supprimé avec succès.');
                this.loadFiles();
            },
            error: () => {
                this.errorMessage.set('Impossible de supprimer le fichier.');
            }
        });
    }

    setFilter(filter: 'ALL' | 'VALID' | 'EXPIRED'): void {
        this.filter.set(filter);
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

  openFile(file: StoredFileList): void {
    if (file.downloadToken) {
        this.router.navigate(['/downloads', file.downloadToken]);
    }
  }
  goToUpload(): void {
        this.router.navigate(['/upload']);
    }
 }