package com.example.emptya;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ModifierPretDialogFragment extends DialogFragment {
    private EditText editTextId;
    private EditText editTextNom;
    private EditText editTextBanque;
    private EditText editTextMontant;
    private EditText editTextDate;
    private EditText editTextTaux;
    private Button buttonSave;
    private Button buttonCancel;

    private String id;
    private String nom;
    private String banque;
    private int montant;
    private String date;
    private double taux;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modifier_pret_dialog, container, false);

        editTextId = view.findViewById(R.id.editTextId);
        editTextNom = view.findViewById(R.id.editTextName);
        editTextBanque = view.findViewById(R.id.editTextBank);
        editTextMontant = view.findViewById(R.id.editTextAmount);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextTaux = view.findViewById(R.id.editTextTaux);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        // Récupérez les données du prêt passées depuis MainActivity
        Bundle args = getArguments();
        if (args != null) {
            id = args.getString("id");
            nom = args.getString("nom");
            banque = args.getString("banque");
            montant = args.getInt("montant");
            date = args.getString("date");
            taux = args.getDouble("taux");

            // Pré-remplissez les champs avec les données du prêt
            editTextId.setText(id);
            editTextNom.setText(nom);
            editTextBanque.setText(banque);
            editTextMontant.setText(String.valueOf(montant));
            editTextDate.setText(date);
            editTextTaux.setText(String.valueOf(taux));
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePret();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        editTextDate.setOnClickListener(v -> showDatePickerDialog());
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
        // Obtenez les nouvelles valeurs des champs
        String nouveauNom = editTextNom.getText().toString().trim();
        String nouvelleBanque = editTextBanque.getText().toString().trim();
        int nouveauMontant = Integer.parseInt(editTextMontant.getText().toString());
        String nouvelleDate = editTextDate.getText().toString().trim();
        double nouveauTaux = Double.parseDouble(editTextTaux.getText().toString());
        String n_date = nouvelleDate;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date date = null;
        try {
            date = inputFormat.parse(nouvelleDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Format the parsed date into the desired output format
        n_date = outputFormat.format(date);

        // Appelez une méthode pour mettre à jour le prêt dans l'API
        ((MainActivity) getActivity()).updateLoan(id, nouveauNom, nouvelleBanque, nouveauMontant, n_date, nouveauTaux);
        MainActivity mainActivity = (MainActivity) getActivity();
        //if (mainActivity != null) {
            mainActivity.refreshLoanList();
        //}
        dismiss();
    }
}

