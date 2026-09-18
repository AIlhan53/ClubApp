import {Component, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {RegisterService} from '../../Service/register-service';
import Swal from 'sweetalert2';
import {registerRequest} from '../../Interface/registerRequest';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
  standalone: true
})
export class Register {

  registerForm: FormGroup;
  loading = signal(false);
  errorMessage = signal('');
  showPassword = signal(false);

  rolesList = [
    {name: "RESPONSABLE"},
    {name: "MEMBRE"}
  ];

  constructor(private fb: FormBuilder,
              private registerService: RegisterService,
              private router: Router) {

    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!?]).+$/)
      ]],
      role: ['', Validators.required]
    });
  }


  togglePasswordVisibility(): void {
    this.showPassword.update(v => !v);
  }

  selectedRole: string | null = null;

  toggleRole(roleName: string): void {
    // Si on clique sur le rôle déjà sélectionné, on le désélectionne
    if (this.selectedRole === roleName) {
      this.selectedRole = null;
    } else {
      // Sinon on remplace l'ancien rôle par le nouveau
      this.selectedRole = roleName;
    }

    this.registerForm.get('role')?.setValue(this.selectedRole);
  }

  isSelected(roleName: string): boolean {
    return this.selectedRole === roleName;
  }

  onSubmit(): void {
    if (!this.registerForm.valid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    const request: registerRequest = this.registerForm.value;

    this.registerService.register(request).subscribe({
      next: (msg) => {
        this.loading.set(false);
        Swal.fire({
          icon: 'success',
          title: 'Sign up successful',
          text: 'Please check your email to activate your account.',
          confirmButtonText: 'OK'
        }).then(() => this.router.navigate(['/login']));
      },
      error: (err) => {
        this.loading.set(false);
        const backendMsg = err.error?.error ?? err.error?.message ?? 'An unexpected error occurred.';

        Swal.fire({
          icon: 'error',
          title: 'Registration error',
          text: backendMsg,
          confirmButtonText: 'OK'
        });
      }
    });
  }
}
