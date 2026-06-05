import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ModuleItem, Profile, ProfilesService } from '../../../core/services/profiles';

@Component({
  selector: 'app-perfiles-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './perfiles-page.html',
  styleUrl: './perfiles-page.css',
})
export class PerfilesPage implements OnInit {
  profiles: Profile[] = [];
  modules: ModuleItem[] = [];
  profileModalOpen = false;
  permissionsModalOpen = false;
  selectedProfileId: number | null = null;
  selectedProfileName = '';
  selectedModules: number[] = [];
  errorMessage = '';
  editingProfileId: number | null = null;

  form = {
    nombre: '',
    descripcion: '',
  };

  constructor(
    private profilesService: ProfilesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadProfiles();
    this.profilesService.listModules().subscribe({
      next: (modules) => {
        this.modules = modules;
        this.cdr.detectChanges();
      },
    });
  }

  loadProfiles() {
    this.profilesService.listProfiles().subscribe({
      next: (profiles) => {
        this.profiles = profiles;
        this.cdr.detectChanges();
      },
    });
  }

  openProfileModal() {
    this.profileModalOpen = true;
    this.errorMessage = '';
  }

  editProfile(profile: Profile) {
    this.editingProfileId = profile.id;
    this.form = {
      nombre: profile.nombre,
      descripcion: profile.descripcion,
    };
    this.openProfileModal();
  }

  toggleProfileStatus(profile: Profile) {
    this.errorMessage = '';
    this.profilesService.toggleProfileStatus(profile.id).subscribe({
      next: (updatedProfile) => {
        this.profiles = this.profiles.map((item) =>
          item.id === profile.id ? updatedProfile : item
        );
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo cambiar el estado del perfil.';
        this.cdr.detectChanges();
      },
    });
  }

  deleteProfile(profile: Profile) {
    this.errorMessage = '';
    this.profilesService.deleteProfile(profile.id).subscribe({
      next: () => {
        this.profiles = this.profiles.filter((item) => item.id !== profile.id);
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar el perfil.';
        this.cdr.detectChanges();
      },
    });
  }

  closeProfileModal() {
    this.profileModalOpen = false;
    this.errorMessage = '';
    this.editingProfileId = null;
    this.form = { nombre: '', descripcion: '' };
  }

  openPermissionsModal(profile: Profile) {
    this.selectedProfileId = profile.id;
    this.selectedProfileName = profile.nombre;
    this.selectedModules = [...profile.moduloIds];
    this.permissionsModalOpen = true;
    this.errorMessage = '';
  }

  closePermissionsModal() {
    this.permissionsModalOpen = false;
    this.errorMessage = '';
    this.selectedProfileId = null;
    this.selectedProfileName = '';
    this.selectedModules = [];
  }

  saveProfile() {
    if (!this.form.nombre.trim()) {
      this.errorMessage = 'El nombre del perfil es obligatorio.';
      return;
    }

    const request$ = this.editingProfileId
      ? this.profilesService.updateProfile(this.editingProfileId, this.form)
      : this.profilesService.createProfile(this.form);

    request$.subscribe({
      next: (profile) => {
        this.profiles = [profile, ...this.profiles.filter((item) => item.id !== profile.id)];
        this.closeProfileModal();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo guardar el perfil.';
        this.cdr.detectChanges();
      },
    });
  }

  toggleModule(moduleId: number, checked: boolean) {
    if (checked) {
      this.selectedModules = this.selectedModules.includes(moduleId)
        ? this.selectedModules
        : [...this.selectedModules, moduleId];
      return;
    }
    this.selectedModules = this.selectedModules.filter((id) => id !== moduleId);
  }

  savePermissions() {
    if (!this.selectedProfileId) {
      return;
    }
    this.profilesService.updateModules(this.selectedProfileId, this.selectedModules).subscribe({
      next: (profile) => {
        this.profiles = this.profiles.map((item) => (item.id === profile.id ? profile : item));
        this.closePermissionsModal();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudieron guardar los modulos.';
        this.cdr.detectChanges();
      },
    });
  }
}
