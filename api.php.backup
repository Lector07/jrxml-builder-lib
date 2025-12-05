<?php

use App\Http\Controllers\Api\V1;
use App\Http\Controllers\Api\V1\Admin;
use App\Http\Controllers\Api\V1\Auth\ForgotPasswordLinkController;
use App\Http\Controllers\Api\V1\Auth\PasswordController;
use App\Http\Controllers\Api\V1\Auth\VerifyEmailController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\V1\Admin\AdminDoctorController;
use App\Http\Controllers\Api\V1\Admin\AdminProcedureCategoryController;
use App\Http\Controllers\Api\V1\Admin\AdminUserController;
use App\Http\Controllers\Api\V1\PatientAppointmentController;

// routes/api.php

Route::get('/email/verify', function () {
    return redirect('/#/settings/profile?verified=1');
})->middleware('auth:sanctum')->name('verification.notice');

Route::get('/email/verify/{id}/{hash}', [VerifyEmailController::class, 'verify'])
    ->middleware(['signed'])
    ->name('verification.verify');

Route::post('/email/verification-notification', [VerifyEmailController::class, 'resend'])
    ->middleware(['auth:sanctum', 'throttle:6,1'])
    ->name('verification.send');


Route::prefix('v1')->name('v1.')->group(function () {
    // Trasy publiczne - Auth (guest)
    Route::post('/register', V1\Auth\RegisterController::class)
        ->middleware('guest')
        ->name('register');

    Route::post('/login', [V1\Auth\LoginController::class, 'store'])
        ->middleware('guest')
        ->name('login');

    Route::post('/forgot-password', [ForgotPasswordLinkController::class, 'store'])
        ->middleware('guest')
        ->name('password.email');

    Route::post('/reset-password', [V1\Auth\NewPasswordController::class, 'store'])
        ->middleware('guest')
        ->name('password.reset');

    // Trasy publiczne - Procedures & Doctors
    Route::get('/procedures/categories', [V1\ProcedureController::class, 'categories'])
        ->name('procedures.categories');

    Route::apiResource('/procedures', V1\ProcedureController::class)
        ->only(['index', 'show'])
        ->names('procedures');

    Route::apiResource('/doctors', V1\DoctorController::class)
        ->only(['index', 'show'])
        ->names('doctors.public');

    Route::get('/appointments/check-availability', [V1\PatientAppointmentController::class, 'checkAvailability'])
        ->name('appointments.check');

    Route::get('/doctors/{doctor}/availability', [V1\DoctorController::class, 'getAvailability'])
        ->name('doctors.availability');

    Route::get('/doctors/{doctor}/booked-appointments', [V1\DoctorController::class, 'getBookedAppointments'])
        ->name('doctors.booked-appointments');


    // Trasy wymagające uwierzytelnienia
    Route::middleware('auth:sanctum')->group(function () {
        Route::post('/logout', [V1\Auth\LogoutController::class, 'destroy'])
            ->name('logout');

        Route::get('/user', function (Request $request) {
            \Illuminate\Support\Facades\Log::info('GET /api/v1/user route hit with UserResource. User ID: ' . $request->user()->id . ', Role: ' . $request->user()->role . ', Path: ' . $request->user()->profile_picture_path);
            return new \App\Http\Resources\Api\V1\UserResource($request->user());
        })->name('user.show');

        Route::get('/user/profile', [V1\UserProfileController::class, 'show'])
            ->name('user.profile.show');

        Route::put('/user/profile', [V1\UserProfileController::class, 'update'])
            ->name('user.profile.update');

        Route::post('/user/profile/avatar', [V1\UserProfileController::class, 'updateAvatar'])
            ->name('user.profile.avatar.update');

        Route::delete('/user/profile/avatar', [V1\UserProfileController::class, 'deleteAvatar'])
            ->name('user.profile.avatar.delete');

        Route::put('/user/password', [V1\UserProfileController::class, 'updatePassword'])
            ->name('user.password.update');

        Route::delete('/user/profile', [V1\UserProfileController::class, 'destroy'])
            ->name('user.profile.destroy');

        Route::apiResource('/patient/appointments', V1\PatientAppointmentController::class)
            ->except(['update'])
            ->names('patient.appointments');

        // Trasy dla lekarzy
        Route::prefix('doctor')
            ->middleware('auth.doctor')
            ->name('doctor.')
            ->group(function () {
                Route::get('/dashboard', [V1\Doctor\DoctorDashboardController::class, 'index'])
                    ->name('dashboard.index');

                Route::get('/dashboard-data', [V1\Doctor\DoctorDashboardController::class, 'index'])
                    ->name('dashboard.data');

                Route::get('/profile', [V1\Doctor\DoctorProfileController::class, 'show'])
                    ->name('profile.show');

                Route::put('/profile', [V1\Doctor\DoctorProfileController::class, 'update'])
                    ->name('profile.update');

                Route::post('/profile/avatar', [V1\Doctor\DoctorProfileController::class, 'updateAvatar'])
                    ->name('profile.avatar.update');

                Route::delete('/profile/avatar', [V1\Doctor\DoctorProfileController::class, 'deleteAvatar'])
                    ->name('profile.avatar.delete');

                Route::get('/appointments', [V1\Doctor\DoctorAppointmentController::class, 'index'])
                    ->name('appointments.index');

                Route::get('/appointments/{appointment}', [V1\Doctor\DoctorAppointmentController::class, 'show'])
                    ->name('appointments.show');

                Route::put('/appointments/{appointment}', [V1\Doctor\DoctorAppointmentController::class, 'update'])
                    ->name('appointments.update');

                Route::post('/appointments/report', [V1\Doctor\DoctorAppointmentController::class, 'generateReport']);

                Route::get('/schedule/events', [V1\Doctor\DoctorAppointmentController::class, 'getScheduleEvents'])
                    ->name('schedule.events');

                Route::apiResource('/procedures', Admin\AdminProcedureController::class)
                    ->names('procedures');
            });
    });
});

Route::prefix('v1/admin')
    ->middleware(['auth:sanctum', 'auth.admin'])
    ->name('admin.')
    ->group(function () {
        Route::post('/appointments/report', [Admin\AdminAppointmentController::class, 'generateAppointmentsReport'])
            ->name('appointments.report');
        Route::post('/appointments/report', [Admin\AdminAppointmentController::class, 'generateAppointmentsReport']);

        Route::get('/doctors/report', [Admin\AdminDoctorController::class, 'generateDoctorsReport'])
            ->name('doctors.report');

        Route::put('/user/password', [PasswordController::class, 'update'])
            ->name('password.update');

        Route::get('/dashboard', [Admin\AdminDashboardController::class, 'index'])
            ->name('dashboard');

        Route::get('/procedures/categories', [Admin\AdminProcedureController::class, 'categories'])
            ->name('procedures.categories');

        Route::apiResource('/users', Admin\AdminUserController::class)
            ->names('users');

        Route::post('/users/{user}/avatar', [Admin\AdminUserController::class, 'updateAvatar'])
            ->name('users.avatar.update');

        Route::delete('/users/{user}/avatar', [AdminUserController::class, 'deleteAvatar'])
            ->name('users.avatar.delete');

        Route::apiResource('procedure-categories', Admin\AdminProcedureCategoryController::class)
            ->names('procedure-categories');

        Route::apiResource('/doctors', Admin\AdminDoctorController::class)
            ->names('doctors');

        Route::apiResource('/procedures', Admin\AdminProcedureController::class)
            ->names('procedures');

        Route::apiResource('/appointments', Admin\AdminAppointmentController::class)
            ->names('appointments');

        Route::post('/doctors/{doctor}/avatar', [Admin\AdminDoctorController::class, 'updateAvatar'])
            ->name('doctors.avatar.update');
        Route::delete('/doctors/{doctor}/avatar', [Admin\AdminDoctorController::class, 'deleteAvatar'])
            ->name('doctors.avatar.delete');
    });

