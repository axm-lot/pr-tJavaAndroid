package com.example.emptya;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    final String API = ApiConfiguration.getApiUrl();
    private ApiService apiService;
    private LinearLayout cardContainer;
    private FloatingActionButton fabAddPret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardContainer = findViewById(R.id.cardContainer);
        fabAddPret = findViewById(R.id.fabAddPret);

        Gson gson = new GsonBuilder().setLenient().create();
        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfiguration.getApiUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        apiService = retrofit.create(ApiService.class);

        fabAddPret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPretDialog();
            }
        });

        fabAddPret.setContentDescription(getString(R.string.add_pret));
        // Call the method to perform the GET request
        new FetchDataTask().execute();
    }
    private void openAddPretDialog() {
        AddPretDialogFragment dialogFragment = new AddPretDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "AddPretDialogFragment");
    }

    public class FetchDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            // URL of your Node.js server
            String urlStr = API + "prets";

            try {
                URL url = new URL(urlStr);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    TextView textMin = findViewById(R.id.textMin);
                    TextView textMax = findViewById(R.id.textMax);
                    TextView textSum = findViewById(R.id.textSum);
                    double min = Double.POSITIVE_INFINITY;
                    double max = Double.NEGATIVE_INFINITY;
                    double sum = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.optString("_id");
                        String nom = jsonObject.optString("nom_client");
                        String banque = jsonObject.optString("nom_banque");
                        int montant = jsonObject.optInt("montant");
                        String date = jsonObject.optString("date_pret");
                        double taux = jsonObject.optDouble("taux_pret");
                        double somme = montant*(1+taux);
                        double montantPret = montant ;
                        String fdate = ConvertDateFrench.convertDate(date);
                        min = Math.min(min, montantPret);
                        max = Math.max(max, montantPret);
                        sum += montantPret;

                        // Inflate card_layout.xml for each loan
                        View cardView = LayoutInflater.from(MainActivity.this).inflate(R.layout.card_layout, cardContainer, false);

                        TextView textId = cardView.findViewById(R.id.textId);
                        TextView textNom = cardView.findViewById(R.id.textNom);
                        TextView textBanque = cardView.findViewById(R.id.textBanque);
                        TextView textMontant = cardView.findViewById(R.id.textMontant);
                        TextView textDate = cardView.findViewById(R.id.textDate);
                        TextView textTaux = cardView.findViewById(R.id.textTaux);
                        textId.setVisibility(View.GONE);
                        textId.setText("Nom: " + id);
                        textNom.setText("Nom: " + nom);
                        textBanque.setText("Banque: " + banque);
                        textMontant.setText("Montant: " + montantPret);
                        textDate.setText("Date: " + fdate);
                        textTaux.setText("Somme à payer: " + somme);

                        ImageButton btnModifier = cardView.findViewById(R.id.btnModifier);
                        ImageButton btnSupprimer = cardView.findViewById(R.id.btnSupprimer);

                        // Set onClickListeners for Modifier and Supprimer buttons
                        btnModifier.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                modifierPret(id, nom, banque, montant, date, taux);
                            }
                        });

                        btnSupprimer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                supprimerPret(id);
                            }
                        });
                        // Add the card to the container
                        cardContainer.addView(cardView);
                    }
                    textMin.setText("Minimum: " + min +" Ar");
                    textMax.setText("Maximum: " + max + " Ar");
                    textSum.setText("Total: " + sum + " Ar");
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing error
                    TextView errorText = new TextView(MainActivity.this);
                    errorText.setText("Error parsing JSON");
                    cardContainer.addView(errorText);
                }
            } else {
                TextView errorText = new TextView(MainActivity.this);
                //errorText.setText("Error fetching data from : " + API);
                cardContainer.addView(errorText);
            }
        }
    }
    public void modifierPret(String id, String nom, String banque, int montant, String date, double taux) {
        // Ouvrir la fenêtre modale pour modifier le prêt
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("nom", nom);
        args.putString("banque", banque);
        args.putInt("montant", montant);
        args.putString("date", date);
        args.putDouble("taux", taux);

        ModifierPretDialogFragment dialogFragment = new ModifierPretDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "ModifierPretDialogFragment");
    }

    public void supprimerPret(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer ce prêt ?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call API to delete the loan
                deleteLoanFromApi(id);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Annuler la suppression si l'utilisateur clique sur "Non"
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteLoanFromApi(String id) {
        // Call your API service to delete the loan
        Call<JsonObject> call = apiService.deleteLoan(id);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    // If deletion is successful, show a toast or perform any other action
                    Toast.makeText(MainActivity.this, "Prêt supprimé avec succès", Toast.LENGTH_SHORT).show();

                    refreshLoanList();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur lors de la suppression du prêt", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Prêt supprimé avec succès", Toast.LENGTH_SHORT).show();
                refreshLoanList();
            }
        });
    }
    public void refreshLoanList() {
        cardContainer.removeAllViews();
        new FetchDataTask().execute();
    }

    public void updateLoan(String id, String nom, String banque, int montant, String date, double taux) {
        JsonObject updatedLoan = new JsonObject();
        updatedLoan.addProperty("id", id);
        updatedLoan.addProperty("nom_client", nom);
        updatedLoan.addProperty("nom_banque", banque);
        updatedLoan.addProperty("montant", montant);
        updatedLoan.addProperty("date_pret", date);
        updatedLoan.addProperty("taux_pret", taux);
        Call<JsonObject> call = apiService.updateLoan(id, updatedLoan);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    // Si la mise à jour réussit, affichez un message ou effectuez une autre action
                    Toast.makeText(MainActivity.this, "Prêt mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    // Rafraîchissez la liste des prêts si nécessaire
                    refreshLoanList();
                } else {
                    // Si la mise à jour échoue, affichez un message d'erreur
                    Toast.makeText(MainActivity.this, "Erreur lors de la mise à jour du prêt", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Si une erreur réseau se produit, affichez un message d'erreur
                Toast.makeText(MainActivity.this, "Modification prise en compte ", Toast.LENGTH_SHORT).show();
                refreshLoanList();
            }
        });
    }

}
