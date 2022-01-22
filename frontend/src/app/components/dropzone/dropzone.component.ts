import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Output } from '@angular/core';
import { SuccessUploadResponse } from 'src/app/app.component';

@Component({
  selector: 'app-dropzone',
  templateUrl: './dropzone.component.html',
  styleUrls: ['./dropzone.component.scss']
})
export class DropzoneComponent {

  @Output() onUploadSuccessEvent = new EventEmitter<SuccessUploadResponse>();
  @Output() onUploadErrorEvent = new EventEmitter<HttpErrorResponse>();

  constructor(private http: HttpClient) { }

  uploadFile(files: File[]) {
    if (files.length > 1) {
      console.log("Seulement 1 fichier peut être upload à la fois");
    }

    const formData = new FormData();
    formData.append('file', files[0]);

    this.http.post<SuccessUploadResponse>("http://localhost:8000/api/public/file/upload", formData).subscribe(
      (res: SuccessUploadResponse) => this.onUploadSuccessEvent.emit(res),
      (err: HttpErrorResponse) => this.onUploadErrorEvent.emit(err)
    )
  }

}
