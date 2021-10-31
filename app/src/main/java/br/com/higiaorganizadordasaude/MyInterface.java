package br.com.higiaorganizadordasaude;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import androidx.activity.result.ActivityResultLauncher;

public interface MyInterface {
    public void RetornoModal(boolean a);
    ActivityResultLauncher <Intent> activityResultLauncher = null;
}
