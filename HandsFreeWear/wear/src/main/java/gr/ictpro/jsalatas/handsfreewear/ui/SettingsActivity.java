package gr.ictpro.jsalatas.handsfreewear.ui;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import gr.ictpro.jsalatas.handsfreewear.R;
import gr.ictpro.jsalatas.handsfreewear.application.HandsFreeWearApplication;
import gr.ictpro.jsalatas.handsfreewear.application.Settings;

public class SettingsActivity extends WearableActivity {
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        settings = HandsFreeWearApplication.getSettings();
        SeekBar accuracy = findViewById(R.id.accuracy);
        final Switch switchVibrate = findViewById(R.id.switch_vibrate);
        final Switch switchHighlight = findViewById(R.id.switch_highlight_selected);

        LinearLayout vibrate = findViewById(R.id.vibrate);
        LinearLayout highlight = findViewById(R.id.highlight_selected);

        accuracy.setProgress(settings.getAccuracy() - 3);
        switchVibrate.setChecked(settings.getVibrate());
        switchHighlight.setChecked(settings.getHighlightSelected());


        accuracy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                settings.setAccuracy(progress + 3);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setVibrate(!settings.getVibrate());
                switchVibrate.setChecked(settings.getVibrate());
            }
        });

        highlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setHighlightSelected(!settings.getHighlightSelected());
                switchHighlight.setChecked(settings.getHighlightSelected());

            }
        });


    }
}
