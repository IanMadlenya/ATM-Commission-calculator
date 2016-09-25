package pro.kinect.atmcc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.orange_box.storebox.StoreBox;

import pro.kinect.atmcc.Utils.Prefs;

public class MainActivity extends AppCompatActivity {

    private TextView tvCentralBankExchangeRate;
    private EditText etStartBalance;
    private EditText etFinishBalance;
    private EditText etAmountFromATM;
    private Context context;
    private double centralExchangeRate = 0;
    private EditText etSetCentralExchangeRate;
    private TextView tvResult;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        prefs = StoreBox.create(context, Prefs.class);

        setToolBarAndFAB();

        setUI();

    }

    private void setToolBarAndFAB() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Animation animationFAB = AnimationUtils.loadAnimation(this, R.anim.fab_rotate_05sec);
        final ImageView ivAutorenew = (ImageView) findViewById(R.id.ivAutorenew);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivAutorenew.startAnimation(animationFAB);
                if (centralExchangeRate == 0) {
                    dialogExchangeRate();
                } else {
                    calculate();
                }

            }
        });
    }

    private void setUI() {
        tvCentralBankExchangeRate = (TextView) findViewById(R.id.tvCenralBankExchangeRate);
        etStartBalance = (EditText) findViewById(R.id.etStartBalance);
        etFinishBalance = (EditText) findViewById(R.id.etFinishCost);
        etAmountFromATM = (EditText) findViewById(R.id.etAmountFromATM);
        tvResult = (TextView) findViewById(R.id.tvResult);

        tvCentralBankExchangeRate.setClickable(true);
        tvCentralBankExchangeRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogExchangeRate();
            }
        });


    }

    private void dialogExchangeRate() {
        View viewForDialog = LayoutInflater.from(context).inflate(R.layout.dialog_local_excange_rate, null);
        etSetCentralExchangeRate = (EditText) viewForDialog.findViewById(R.id.etLocalExcangeRate);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setView(viewForDialog);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                parseCentralExchangeRate();
                dialog.dismiss();
            }
        });
        final Dialog dialog = alertDialog.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        if (centralExchangeRate > 0) {
            etSetCentralExchangeRate.setText(String.valueOf(centralExchangeRate));
            etSetCentralExchangeRate.setSelection(String.valueOf(centralExchangeRate).length());
        } else {
            etSetCentralExchangeRate.setHint(String.valueOf(centralExchangeRate));
            etSetCentralExchangeRate.setSelection(0);
        }

        etSetCentralExchangeRate.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    parseCentralExchangeRate();
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        dialog.show();
    }

    private void parseCentralExchangeRate() {
            String string = String.valueOf(etSetCentralExchangeRate.getText());
        try {
            centralExchangeRate = Double.parseDouble(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            centralExchangeRate = 0;
        }
        prefs.setCentralExchangeRate(centralExchangeRate);
            initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUI();
    }

    private void initUI() {
        try {
            centralExchangeRate = prefs.getCentralExchangeRate();
        } catch (Exception e) {
            e.printStackTrace();
            centralExchangeRate = 0;
            prefs.setCentralExchangeRate(0);
        }

        String localExchangeRateString = String.format(
                context.getResources().getString(R.string.central_bank_exchange_rate)
                + ": " +
                "%.2f", centralExchangeRate);

        tvCentralBankExchangeRate.setText(localExchangeRateString);
    }

    private void calculate() {
        String startCostString = null;
        String finishCostString = null;
        String amountFromATMString = null;
        try {
            startCostString = etStartBalance.getText().toString();
            finishCostString = etFinishBalance.getText().toString();
            amountFromATMString = etAmountFromATM.getText().toString();
        } catch (Exception e) {
            Log.e(Const.LOG, "Empty fields");
            e.printStackTrace();
        }

        double startCost = 0;
        double finishCost = 0;
        double amountFromATM = 0;
        try {
            startCost = TextUtils.isEmpty(startCostString) ? 0 : Double.parseDouble(startCostString);
            finishCost = TextUtils.isEmpty(finishCostString) ? 0 : Double.parseDouble(finishCostString);
            amountFromATM = TextUtils.isEmpty(amountFromATMString) ? 0 : Double.parseDouble(amountFromATMString);

        } catch (NumberFormatException e) {
            Log.e(Const.LOG, "Parse error in calculate");
            e.printStackTrace();
        }

        double actualRate = amountFromATM / (startCost - finishCost);
        double lostSum = (centralExchangeRate * (startCost - finishCost)) - (actualRate * (startCost - finishCost));
        double exchange = (lostSum * 100) / (centralExchangeRate * (startCost - finishCost));

        String calculate_result = context.getResources().getString(R.string.calculate_result);
        String result = String.format(calculate_result + "%%", actualRate, lostSum, exchange);
        tvResult.setText(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_central_rate :
                dialogExchangeRate();
                return true;
            case R.id.action_clear_fields:
                clearField();
                return true;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearField() {
        etAmountFromATM.setText("");
        etAmountFromATM.setHint("0.00");

        etStartBalance.setText("");
        etStartBalance.setHint("0.00");

        etFinishBalance.setText("");
        etFinishBalance.setHint("0.00");

        tvResult.setText(R.string.result);
    }
}
