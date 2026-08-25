
import { CommonModule } from '@angular/common';

import { HeaderComponent } from '../../shared/header/header.component';


import { ActivatedRoute } from '@angular/router';


import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { DownloadApiService } from '../../core/service/download-api.service';
import { DownloadInfo } from '../../core/models/download-info.model';



@Component({
  selector: 'app-download',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HeaderComponent],
  templateUrl: './download.component.html',
  styleUrl: './download.component.css'
})
export class DownloadComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private downloadApiService = inject(DownloadApiService);
  private route = inject(ActivatedRoute);


    private downloadToken = '';

    downloadForm: FormGroup = this.formBuilder.group({
        password: ['']
    });

    downloadInfo = signal<DownloadInfo | null>(null);
    loading = signal(false);
    downloading = signal(false);
    errorMessage = signal('');


    ngOnInit(): void {
        const token = this.route.snapshot.paramMap.get('token');

        if (!token) {
            this.errorMessage.set('Lien de téléchargement invalide.');
            return;
        }

        this.downloadToken = token;
        this.loading.set(true);

        this.downloadApiService.getDownloadInfo(token).subscribe({
            next: (info) => {
            this.downloadInfo.set(info);
            this.loading.set(false);
            },
             error: (error) => {
                this.loading.set(false);
                this.errorMessage.set(this.resolveErrorMessage(error));
            }
        });
    }

    onDownload(): void {
        const info = this.downloadInfo();

        if (!info) {
            return;
        }

        const password = this.downloadForm.get('password')?.value || null;

        this.downloading.set(true);
        this.errorMessage.set('');

        this.downloadApiService
            .downloadFile(this.downloadToken, password)
            .subscribe({
                next: (blob) => {
                    this.downloading.set(false);

                    const url = URL.createObjectURL(blob);
                    const link = document.createElement('a');

                    link.href = url;
                    link.download = info.fileName;

                    document.body.appendChild(link);
                    link.click();
                    link.remove();

                    URL.revokeObjectURL(url);
                },
                error: (error) => {
                    this.downloading.set(false);
                    this.errorMessage.set(this.resolveErrorMessage(error));
                }
            });
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

 
  private resolveErrorMessage(error: any): string {
  switch (error.status) {
    case 0:
      return 'Impossible de contacter le serveur.';
    case 403:
      return 'Mot de passe incorrect.';
    case 404:
      return 'Lien de téléchargement invalide.';
    case 410:
      return 'Ce fichier a expiré.';
    default:
      return 'Une erreur est survenue.';
    }
  }
}

