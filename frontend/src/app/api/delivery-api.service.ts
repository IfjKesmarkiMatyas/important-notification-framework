import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Channel, DeliveryJobView, TestDeliveryResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class DeliveryApiService {
  private readonly http = inject(HttpClient);

  jobs(): Observable<DeliveryJobView[]> {
    return this.http.get<DeliveryJobView[]>('/api/admin/delivery/jobs');
  }

  test(channel: Channel, recipient: string): Observable<TestDeliveryResponse> {
    return this.http.post<TestDeliveryResponse>('/api/admin/delivery/test', { channel, recipient });
  }
}
