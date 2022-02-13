import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';

export interface ResponseDialog {
  successUpload: SuccessUploadResponse | null,
  errorUpload: HttpErrorResponse | null
}
export interface SuccessUploadResponse {
  link: string;
}

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent {
  title = 'clipystream-front';

  constructor() {
    // TODO remove me
    // this.uploadResponseDialog.success = {
    //   link: "https://google.com"
    // }

    // this.uploadResponseDialog.error = {
    //   error: "Bad Request"
    // }
  }

  uploadResponseDialog: ResponseDialog = {
    successUpload: null,
    errorUpload: null
  }

  showUploadSuccess(successResponse: SuccessUploadResponse): void {
    // TODO
    console.log(successResponse);
    this.setSuccessUpload(successResponse);

  }

  showUploadError(errorResponse: HttpErrorResponse): void {
    // TODO
    console.log(errorResponse);
    this.setErrorUpload(errorResponse);
  }

  onCopiedLinkToClipboard(e: Event) {
    // TODO
    console.log(e);
  }

  private setSuccessUpload(data: SuccessUploadResponse) {
    this.uploadResponseDialog.successUpload = data;
    this.uploadResponseDialog.errorUpload = null;
  }

  private setErrorUpload(data: HttpErrorResponse) {
    this.uploadResponseDialog.successUpload = null;
    this.uploadResponseDialog.errorUpload = data;
  }

}
