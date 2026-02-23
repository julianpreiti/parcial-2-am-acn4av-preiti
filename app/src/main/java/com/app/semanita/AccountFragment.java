package com.app.semanita;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AccountFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore db;
    Button buttonLogout, buttonSaveName;
    TextView textViewEmail;
    EditText editTextName;
    FirebaseUser user;
    private String originalName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        buttonLogout = view.findViewById(R.id.logout);
        buttonSaveName = view.findViewById(R.id.button_save_name);
        textViewEmail = view.findViewById(R.id.user_details);
        editTextName = view.findViewById(R.id.edit_name);
        user = auth.getCurrentUser();

        // El boton de cambiar nombre empieza deshabilitado
        buttonSaveName.setEnabled(false);
        buttonSaveName.setBackgroundTintList(requireContext().getColorStateList(R.color.inactive));
        if (user == null) {
            Intent intent = new Intent(requireActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        } else {
            textViewEmail.setText(user.getEmail());
            loadUserName();
        }

        // Usamos Textwacher para detectar cambios
        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s.toString().trim();
                boolean hasChanges = !currentText.equals(originalName) && !currentText.isEmpty();

                buttonSaveName.setEnabled(hasChanges);
                if (hasChanges) {
                    buttonSaveName.setBackgroundTintList(requireContext().getColorStateList(R.color.alter));
                } else {
                    buttonSaveName.setBackgroundTintList(requireContext().getColorStateList(R.color.inactive));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonSaveName.setOnClickListener(v -> saveUserName());

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void loadUserName() {
        String uid = Objects.requireNonNull(user).getUid();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        String name = documentSnapshot.getString("name");
                        originalName = name != null ? name : "";
                        editTextName.setText(originalName);
                    } else {
                        originalName = "";
                    }
                });
    }

    private void saveUserName() {
        String name = editTextName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.name_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.length() > 50) {
            Toast.makeText(getContext(), getString(R.string.name_too_long), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que el nombre sea diferente
        if (name.equals(originalName)) {
            Toast.makeText(getContext(), getString(R.string.name_same), Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = Objects.requireNonNull(user).getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", user.getEmail());

        db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    originalName = name;
                    buttonSaveName.setEnabled(false);
                    buttonSaveName.setBackgroundTintList(requireContext().getColorStateList(R.color.inactive));
                    Toast.makeText(getContext(), getString(R.string.name_saved), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(getContext(), getString(R.string.name_save_error), Toast.LENGTH_SHORT).show());
    }
}