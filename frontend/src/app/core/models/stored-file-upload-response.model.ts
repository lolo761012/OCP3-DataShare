export interface StoredFileUploadResponse {
  id: number;
  fileName: string;
  size: number;
  downloadToken: string;
  expiresAt: string;
}