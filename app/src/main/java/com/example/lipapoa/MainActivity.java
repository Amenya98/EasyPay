package com.example.lipapoa;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lipapoa.Model.QRGeoModel;
import com.example.lipapoa.Model.QRURLmodel;
import com.example.lipapoa.Model.QRVcardModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannerView = (ZXingScannerView)findViewById(R.id.zxscan);
        txtResult = (TextView)findViewById(R.id.text_result);


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept This permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {
        processRawResult(rawResult.getText());
        scannerView.startCamera();
    }

    private void processRawResult(String text) {
        if(text.startsWith("BEGIN:")) {
            String[] tockens = text.split("\n");
            QRVcardModel qrvCardModel = new QRVcardModel();
            for (int i = 0; i < tockens.length; i++) {
                if (tockens[i].startsWith("BEGIN:")) {
                    qrvCardModel.setType(tockens[i].substring("BEGIN:".length()));
                } else if (tockens[i].startsWith("N:")) {
                    qrvCardModel.setName(tockens[i].substring("N:".length()));
                } else if (tockens[i].startsWith("ORG:")) {
                    qrvCardModel.setOrg(tockens[i].substring("ORG:".length()));
                } else if (tockens[i].startsWith("TEL:")) {
                    qrvCardModel.setTel(tockens[i].substring("TEL:".length()));
                } else if (tockens[i].startsWith("URL:")) {
                    qrvCardModel.setUrl(tockens[i].substring("URL:".length()));
                } else if (tockens[i].startsWith("EMAIL:")) {
                    qrvCardModel.setEmail(tockens[i].substring("EMAIL:".length()));
                } else if (tockens[i].startsWith("ADR:")) {
                    qrvCardModel.setAddress(tockens[i].substring("ADR:".length()));
                } else if (tockens[i].startsWith("NOTE:")) {
                    qrvCardModel.setNote(tockens[i].substring("NOTE:".length()));
                } else if (tockens[i].startsWith("SUMMARY:")) {
                    qrvCardModel.setSummery(tockens[i].substring("SUMMARY:".length()));
                } else if (tockens[i].startsWith("DTSTART:")) {
                    qrvCardModel.setDtstart(tockens[i].substring("DTSTART:".length()));
                } else if (tockens[i].startsWith("DTEND:")) {
                    qrvCardModel.setDtend(tockens[i].substring("DTEND:".length()));
                }

                txtResult.setText(qrvCardModel.getType());
            }
        }
            else if (text.startsWith("http://")||
            text.startsWith("https://")||
                    text.startsWith("wwww.")){
            QRURLmodel qrurLmodel = new QRURLmodel(text);
            txtResult.setText(qrurLmodel.getUrl());
        }
            else if (text.startsWith("geo: "))
        {
            QRGeoModel qrGeoModel = new QRGeoModel();
            String delims = "[ , ?q= ]+";
            String tokens[] = text.split(delims);

            for (int i = 0; i<tokens.length; i++)
            {
                if (tokens[i].startsWith("geo:"))
                {
                    qrGeoModel.setLat(tokens[i].substring("geo:".length()));
                }
            }

            qrGeoModel.setLat(tokens[0].substring("geo:".length()));
            qrGeoModel.setLng(tokens[1]);
            qrGeoModel.setGeo_place(tokens[2]);

            txtResult.setText(qrGeoModel.getLat()+"/"+qrGeoModel.getLng());
        }
            else
        {
            txtResult.setText(text);
        }
        }
    }

