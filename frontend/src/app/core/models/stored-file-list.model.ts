export interface StoredFileList {
  id: number | null;
  fileName: string;
  size: number;
  uploadedAt: string;
  expiresAt: string;
  status: string;
  downloadToken: string | null;
}