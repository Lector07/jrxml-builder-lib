<?php

namespace App\Http\Controllers\Api\V1\Doctor;

use App\Http\Controllers\Controller;
use App\Http\Requests\Api\V1\Doctor\UpdateDoctorAppointmentRequest;
use App\Http\Resources\Api\V1\AppointmentEventResource;
use App\Http\Resources\Api\V1\AppointmentResource;
use App\Models\Appointment;
use Illuminate\Foundation\Auth\Access\AuthorizesRequests;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;
use Illuminate\Support\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class DoctorAppointmentController extends Controller
{
    use AuthorizesRequests;

    /**
     * Wyświetl listę wizyt przypisanych do lekarza.
     */
    public function index(Request $request): AnonymousResourceCollection
    {
        $user = $request->user();
        $doctor = $user->doctor;

        if (!$doctor) {
            return response()->json(['message' => 'Profil lekarza nie znaleziony.'], 404);
        }

        $query = Appointment::where('doctor_id', $doctor->id)
            ->with(['patient:id,name', 'procedure:id,name']);

        if ($request->filled('status') && $request->input('status') !== 'all') {
            $query->where('status', $request->input('status'));
        }

        if ($request->filled('date_from')) {
            $query->whereDate('appointment_datetime', '>=', $request->input('date_from'));
        }

        if ($request->filled('date_to')) {
            $query->whereDate('appointment_datetime', '<=', $request->input('date_to'));
        }

        if ($request->filled('search')) {
            $searchTerm = $request->input('search');
            $query->whereHas('patient', function ($q) use ($searchTerm) {
                $q->where('name', 'like', "%{$searchTerm}%")
                    ->orWhere('last_name', 'like', "%{$searchTerm}%");
            });
        }

        $sortBy = $request->input('sort_by', 'newest');
        switch ($sortBy) {
            case 'oldest':
                $query->orderBy('appointment_datetime', 'asc');
                break;
            case 'patient_asc':
                $query->join('users as patients', 'appointments.patient_id', '=', 'patients.id')
                    ->orderBy('patients.name', 'asc')
                    ->select('appointments.*');
                break;
            case 'patient_desc':
                $query->join('users as patients', 'appointments.patient_id', '=', 'patients.id')
                    ->orderBy('patients.name', 'desc')
                    ->select('appointments.*');
                break;
            case 'newest':
            default:
                $query->orderBy('appointment_datetime', 'desc');
                break;
        }

        $perPage = (int) $request->input('per_page', 10);
        $appointments = $query->paginate($perPage);

        return AppointmentResource::collection($appointments);
    }

    /**
     * Wyświetl szczegóły konkretnej wizyty.
     */
    public function show(Request $request, Appointment $appointment): AppointmentResource
    {
        $this->authorize('viewByDoctor', $appointment);

        $appointment->load(['patient:id,name,email', 'procedure:id,name,description,base_price']);

        return new AppointmentResource($appointment);
    }

    /**
     * Aktualizuj dane wizyty.
     */
    public function update(UpdateDoctorAppointmentRequest $request, Appointment $appointment): AppointmentResource
    {

        $validatedData = $request->validated();

        $appointment->fill($validatedData);
        $appointment->save();

        return new AppointmentResource($appointment->fresh()->load(['patient', 'procedure']));
    }

    public function getScheduleEvents(Request $request): AnonymousResourceCollection
    {
        //pobranie zaloowanego usera
        $user = $request->user();
        $doctor = $user->doctor;

        //sprawdzenie czy lekarz istnieje
        if (!$doctor) {
            return AppointmentResource::collection(collect());
        }

        //walidacja daty
        $request->validate([
            'start' => 'required|date_format:Y-m-d\TH:i:sP,Y-m-d\TH:i:s.vZ,Y-m-d',
            'end' => 'required|date_format:Y-m-d\TH:i:sP,Y-m-d\TH:i:s.vZ,Y-m-d|after_or_equal:start',
        ]);

        $start = Carbon::parse($request->input('start'))->startOfDay();
        $end = Carbon::parse($request->input('end'))->endOfDay();

        //pobranie wizyt ziuta w podanym zakresie
        $query = Appointment::where('doctor_id', $doctor->id)
            ->with(['patient:id,name', 'procedure:id,name,duration_minutes'])
            ->whereBetween('appointment_datetime', [$start, $end])
            //wykluczenie wizyt co sa anulowane lub zakonczone
            ->whereNotIn('status', ['cancelled_by_patient', 'cancelled_by_clinic', 'completed', 'no_show', 'cancelled']);


        $appointments = $query->orderBy('appointment_datetime', 'asc')->get();

        return AppointmentEventResource::collection($appointments);
    }


    public function generateReport(Request $request): JsonResponse|\Illuminate\Http\Response
    {
        $user = $request->user();
        $doctor = $user->doctor;

        if (!$doctor) {
            return response()->json(['message' => 'Profil lekarza nie znaleziony.'], 404);
        }

        $query = Appointment::where('doctor_id', $doctor->id)
            ->with(['patient:id,name', 'procedure:id,name,base_price']);

        $filters = $request->input('filters', []);

        if (isset($filters['status']) && $filters['status'] !== 'all') {
            $query->where('status', $filters['status']);
        }
        if (!empty($filters['date_from'])) {
            $query->whereDate('appointment_datetime', '>=', $filters['date_from']);
        }
        if (!empty($filters['date_to'])) {
            $query->whereDate('appointment_datetime', '<=', $filters['date_to']);
        }
        if (!empty($filters['search'])) {
            $searchTerm = $filters['search'];
            $query->whereHas('patient', function ($q) use ($searchTerm) {
                $q->where('name', 'like', "%{$searchTerm}%");
            });
        }

        $appointments = $query->orderBy('appointment_datetime', 'desc')->get();
        // --- Koniec budowania zapytania ---


        try {
            $config = $request->input('config');
            if (empty($config)) {
                return response()->json(['error' => 'Brak konfiguracji raportu.'], 422);
            }

            $dataForReport = $appointments->map(function ($appointment) use ($doctor) {
                return [
                    'id' => $appointment->id,
                    'appointment_datetime' => $appointment->appointment_datetime,
                    'status_translated' => $appointment->status_translated,
                    'patient_name' => optional($appointment->patient)->name,
                    'doctor_full_name' => $doctor->full_name,
                    'procedure_name' => optional($appointment->procedure)->name,
                    'procedure_base_price' => optional($appointment->procedure)->base_price,
                    'patient_notes' => $appointment->patient_notes,
                ];
            });

            $payload = [
                'config' => $config,
                'jsonData' => json_encode($dataForReport->toArray()),
            ];

            $response = Http::withBody(json_encode($payload), 'application/json')
                ->timeout(30)
                ->post('http://localhost:8080/api/generate-dynamic-report');

            if ($response->successful()) {
                return response($response->body(), 200, [
                    'Content-Type' => 'application/pdf',
                    'Content-Disposition' => 'attachment; filename="raport_wizyt.pdf"',
                ]);
            } else {
                Log::error('Błąd serwisu raportów (Doctor): ' . $response->body());
                return response()->json([
                    'error' => 'Nie udało się wygenerować raportu',
                    'details' => $response->json() ?? $response->body()
                ], $response->status());
            }

        } catch (\Illuminate\Http\Client\ConnectionException $e) {
            Log::error('Błąd połączenia z serwisem raportującym (Doctor): ' . $e->getMessage());
            return response()->json([
                'error' => 'Nie można połączyć się z serwisem raportującym.',
                'details' => $e->getMessage()
            ], 503); // Service Unavailable
        } catch (\Exception $e) {
            Log::error('Błąd generowania raportu wizyt (Doctor): ' . $e->getMessage());
            return response()->json([
                'error' => 'Wystąpił błąd podczas generowania raportu',
                'details' => $e->getMessage()
            ], 500);
        }
    }

}
