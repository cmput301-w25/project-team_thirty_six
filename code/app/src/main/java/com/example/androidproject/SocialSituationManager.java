package com.example.androidproject;

import android.view.View;
import android.widget.RadioButton;

public class SocialSituationManager {
    private RadioButton radioAlone, radioPair, radioGroup, radioCrowd;
    private RadioButton lastSelectedButton = null;
    private String chosenSituation = null;

    public SocialSituationManager(RadioButton radioAlone, RadioButton radioPair,
                                  RadioButton radioGroup, RadioButton radioCrowd) {
        this.radioAlone = radioAlone;
        this.radioPair = radioPair;
        this.radioGroup = radioGroup;
        this.radioCrowd = radioCrowd;

        setupListeners();
    }

    private void setupListeners() {
        View.OnClickListener situationClickListener = v -> {
            RadioButton clickedButton = (RadioButton) v;
            handleSituationSelection(clickedButton);
        };

        radioAlone.setOnClickListener(situationClickListener);
        radioPair.setOnClickListener(situationClickListener);
        radioGroup.setOnClickListener(situationClickListener);
        radioCrowd.setOnClickListener(situationClickListener);
    }

    private void handleSituationSelection(RadioButton clickedButton) {
        if (clickedButton == lastSelectedButton) {
            clickedButton.setChecked(false);
            lastSelectedButton = null;
            chosenSituation = null;
        } else {
            if (lastSelectedButton != null) {
                lastSelectedButton.setChecked(false);
            }
            clickedButton.setChecked(true);
            lastSelectedButton = clickedButton;

            if (clickedButton == radioAlone) {
                chosenSituation = "Alone";
            } else if (clickedButton == radioPair) {
                chosenSituation = "Pair";
            } else if (clickedButton == radioGroup) {
                chosenSituation = "Group";
            } else if (clickedButton == radioCrowd) {
                chosenSituation = "Crowd";
            }
        }
    }

    public void setSituation(String situation) {
        this.chosenSituation = situation;
        if (situation == null) {
            if (lastSelectedButton != null) {
                lastSelectedButton.setChecked(false);
                lastSelectedButton = null;
            }
            return;
        }

        switch (situation) {
            case "Alone":
                radioAlone.setChecked(true);
                lastSelectedButton = radioAlone;
                break;
            case "Pair":
                radioPair.setChecked(true);
                lastSelectedButton = radioPair;
                break;
            case "Group":
                radioGroup.setChecked(true);
                lastSelectedButton = radioGroup;
                break;
            case "Crowd":
                radioCrowd.setChecked(true);
                lastSelectedButton = radioCrowd;
                break;
        }
    }

    public String getChosenSituation() {
        return chosenSituation;
    }
}