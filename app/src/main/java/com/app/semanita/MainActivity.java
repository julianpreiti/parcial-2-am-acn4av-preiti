package com.app.semanita;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Toast;

import com.app.semanita.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);
        // Acá hacemos el evento del fab pero antes verificamos que esté logueado el usuario.
        binding.fab.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                if (current instanceof HomeFragment) {
                    AddTaskDialogFragment dialog = new AddTaskDialogFragment(((HomeFragment) current)::loadTasks);
                    dialog.show(getSupportFragmentManager(), "AddTaskDialog");
                }
            } else {
                Toast.makeText(this, getString(R.string.error_login_required), Toast.LENGTH_SHORT).show();
            }
        });
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (itemId == R.id.history) {
                replaceFragment(new HistoryFragment());
            } else if (itemId == R.id.graph) {
                replaceFragment(new GraphFragment());
            } else if (itemId == R.id.account) {
                replaceFragment(new AccountFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}