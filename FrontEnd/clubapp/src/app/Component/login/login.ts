import {Component, signal} from '@angular/core';
import {LoginService} from '../../Service/login-service';
import {Router, RouterLink} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {loginRequest} from '../../Interface/loginRequest';
import {tap} from 'rxjs';
import {HttpErrorResponse, HttpStatusCode} from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  loginForm: FormGroup;
  loading= signal(false);
  errorMessage = signal('');
  showPassword = signal(false);

  constructor(
    private fb: FormBuilder,
    private loginService: LoginService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }


  togglePasswordVisibility(): void {
    this.showPassword.set(!this.showPassword());
  }


  async onSubmit(): Promise<void> {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const request: loginRequest = this.loginForm.value;

    this.loginService.login(request).subscribe({

      next: async (response) => {

        const user = await this.loginService.loadCurrentUser();

        tap(response => {
          console.log(response);
        });

        if (user && user.active) {
          this.loading.set(false);
          this.router.navigate(['/dashboard']);
        } else {
          if(!user?.active){
            this.errorMessage.set('Your account is deactivated.');
          }
          else {
            this.errorMessage.set('An unexpected error appeared .');
          }
        }
      },

      error: (err) => {
        console.error('Connection Error', err);
        if (err.status === HttpStatusCode.Unauthorized || err.status === HttpStatusCode.Forbidden) {
          this.errorMessage.set('Wrong password or email. Please retry again.');
        }
        else {
          this.errorMessage.set('Error. Please retry.');
        }
        this.loading.set(false);
      }
    });
  }
}
