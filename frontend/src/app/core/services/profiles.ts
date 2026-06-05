import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, Observable, of, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Profile {
  id: number;
  nombre: string;
  descripcion: string;
  estado: boolean;
  moduloIds: number[];
  modulos: string[];
}

export interface ModuleItem {
  id: number;
  nombre: string;
  ruta: string;
  icono: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class ProfilesService {
  private profilesUrl = `${environment.apiUrl}/perfiles`;
  private modulesUrl = `${environment.apiUrl}/modulos`;
  private profilesCache: Profile[] | null = null;
  private modulesCache: ModuleItem[] | null = null;
  private profilesRequest$: Observable<Profile[]> | null = null;
  private modulesRequest$: Observable<ModuleItem[]> | null = null;

  constructor(private http: HttpClient) {}

  getModulesSnapshot(): ModuleItem[] | null {
    return this.modulesCache;
  }

  listProfiles(): Observable<Profile[]> {
    if (this.profilesCache) {
      return of(this.profilesCache);
    }

    if (!this.profilesRequest$) {
      this.profilesRequest$ = this.http.get<Profile[]>(this.profilesUrl).pipe(
        tap((profiles) => {
          this.profilesCache = profiles;
        }),
        finalize(() => {
          this.profilesRequest$ = null;
        }),
        shareReplay(1)
      );
    }

    return this.profilesRequest$;
  }

  createProfile(payload: { nombre: string; descripcion: string }): Observable<Profile> {
    return this.http.post<Profile>(this.profilesUrl, payload).pipe(
      tap((profile) => {
        this.profilesCache = this.profilesCache ? [profile, ...this.profilesCache] : [profile];
      })
    );
  }

  updateProfile(profileId: number, payload: { nombre: string; descripcion: string }): Observable<Profile> {
    return this.http.put<Profile>(`${this.profilesUrl}/${profileId}`, payload).pipe(
      tap((updatedProfile) => {
        this.profilesCache = (this.profilesCache ?? []).map((profile) =>
          profile.id === profileId ? updatedProfile : profile
        );
      })
    );
  }

  toggleProfileStatus(profileId: number): Observable<Profile> {
    return this.http.patch<Profile>(`${this.profilesUrl}/${profileId}/estado`, {}).pipe(
      tap((updatedProfile) => {
        this.profilesCache = (this.profilesCache ?? []).map((profile) =>
          profile.id === profileId ? updatedProfile : profile
        );
      })
    );
  }

  deleteProfile(profileId: number): Observable<void> {
    return this.http.delete<void>(`${this.profilesUrl}/${profileId}`).pipe(
      tap(() => {
        this.profilesCache = (this.profilesCache ?? []).filter((profile) => profile.id !== profileId);
      })
    );
  }

  updateModules(profileId: number, moduloIds: number[]): Observable<Profile> {
    return this.http.put<Profile>(`${this.profilesUrl}/${profileId}/modulos`, { moduloIds }).pipe(
      tap((updatedProfile) => {
        this.profilesCache = (this.profilesCache ?? []).map((profile) =>
          profile.id === profileId ? updatedProfile : profile
        );
      })
    );
  }

  listModules(): Observable<ModuleItem[]> {
    if (this.modulesCache) {
      return of(this.modulesCache);
    }

    if (!this.modulesRequest$) {
      this.modulesRequest$ = this.http.get<ModuleItem[]>(this.modulesUrl).pipe(
        tap((modules) => {
          this.modulesCache = modules;
        }),
        finalize(() => {
          this.modulesRequest$ = null;
        }),
        shareReplay(1)
      );
    }

    return this.modulesRequest$;
  }
}
