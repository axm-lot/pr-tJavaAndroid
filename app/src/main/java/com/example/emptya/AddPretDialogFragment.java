package com.example.emptya;

import android.app.DatePickerDialog;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class AddPretDialogFragment extends DialogFragment {

    private Retrofit retrofit;
    private ApiService apiService;
    private EditText editTextName;
    private EditText editTextBank;
    private EditText editTextAmount;
    private EditText editTextDate;
    private EditText editTextTaux;

    public AddPretDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pret_dialog, container, false);
        final String api = ApiConfiguration.getApiUrl();
        // Initialisez Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(api)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        // Initialize views
        editTextName = view.findViewById(R.id.editTextName);
        editTextBank = view.findViewById(R.id.editTextBank);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextTaux = view.findViewById(R.id.editTextTaux);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        editTextDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSave.setOnClickListener(v -> savePret());
        buttonCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void showDatePickerDialog() {
        // Get current date
        final Calendar calendar = Calendar.getInstance();

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);

                    // Format selected date into desired format
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String formattedDate = sdf.format(calendar.getTime());

                    // Set selected date to EditText
                    editTextDate.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Show date picker dialog
        datePickerDialog.show();
    }
    private void savePret() {
        String name = editTextName.getText().toString().trim();
        String bank = editTextBank.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();
        String dateStr = editTextDate.getText().toString().trim();
        String taux = editTextTaux.getText().toString().trim();
        String formattedDate = dateStr;

        // Define input date format
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Define output date format
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try {
            // Parse the input date string
            Date date = inputFormat.parse(dateStr);

            // Format the parsed date into the desired output format
            formattedDate = outputFormat.format(date);
        }catch (ParseException e){
            Toast.makeText(getActivity(), "Invalid date to parse:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

            int maxValue = 999999999;
        Random random = new Random();
        int randInt = random.nextInt(maxValue) + 1;
        String num = String.valueOf(randInt);

        if (name.isEmpty() || bank.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty() || taux.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount,tax;
        try {
            amount = Double.parseDouble(amountStr);
            tax = Double.parseDouble(taux);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid amount  or tax format", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject loanData = new JsonObject();
        loanData.addProperty("n_compte", num);
        loanData.addProperty("nom_client", name);
        loanData.addProperty("nom_banque", bank);
        loanData.addProperty("montant", amount);
        loanData.addProperty("date_pret", formattedDate);
        loanData.addProperty("taux_pret", tax);

        // Appelez d'API pour sauvegarder le prêt
        Call<JsonObject> call = apiService.saveLoan(loanData);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(getActivity(), "Loan saved successfully!", Toast.LENGTH_SHORT).show();
                        Log.e("API Response", "Failed : " + errorBody);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.refreshLoanList();
                        }
                        dismiss();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed : ", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(getActivity(), "Insertion réussite  ", Toast.LENGTH_SHORT).show();
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.refreshLoanList();
                }
                dismiss();
            }
        });
    }

    // Loan data structure (replace with your actual model)
    public static class Loan {
        private final String n_compte;
        private final String nom_client;
        private final String nom_banque;
        private final double montant;
        private final String date_pret;
        private final double taux_pret;

        public Loan(String n_compte, String nom_client, String nom_banque, double montant, String date_pret, double taux_pret) {
            this.nom_client = nom_client;
            this.nom_banque = nom_banque;
            this.montant = montant;
            this.date_pret = date_pret;
            this.n_compte = n_compte;
            this.taux_pret = taux_pret;
        }

        // Getters
        public String getN_compte() {
            return n_compte;
        }

        public String getNom() {
            return nom_client;
        }

        public String getBank() {
            return nom_banque;
        }

        public double getAmount() {
            return montant;
        }

        public String getDate() {
            return date_pret;
        }
        public double getTaux_pret(){
            return taux_pret;
        }
    }

    // Method to send a POST request using HttpURLConnection
    private String sendPostRequest(String url, JSONObject postData) {
        try {
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            } else {
                return "Error: " + responseCode;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
