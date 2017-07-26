package turma_android.com.br.appsms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView lblEnviado;
    private TextView lblEntregue;
    private TextView lblRecebido;

    private EditText txtNumero;
    private EditText txtMensagem;

    private ReceptorEnviado receptorEnviado = new ReceptorEnviado();
    private ReceptorEntregue receptorEntregue = new ReceptorEntregue();
    private ReceptorRecebido receptorRecebido = new ReceptorRecebido();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        lblEnviado = (TextView) findViewById(R.id.lblEnviado);
        lblEntregue = (TextView) findViewById(R.id.lblEntregue);
        lblRecebido = (TextView) findViewById(R.id.lblRecebido);

        txtNumero = (EditText) findViewById(R.id.txtNumero);
        txtMensagem = (EditText) findViewById(R.id.txtMensagem);

        registerReceiver(receptorEnviado, new IntentFilter("ENVIADO"));
        registerReceiver(receptorEntregue, new IntentFilter("ENTREGUE"));
        registerReceiver(receptorRecebido, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receptorEnviado);
        unregisterReceiver(receptorEntregue);
        unregisterReceiver(receptorRecebido);
    }

    public void enviarMensagem(View view) {
        SmsManager manager = SmsManager.getDefault();

        manager.sendTextMessage(
                txtNumero.getText().toString(),
                null,
                txtMensagem.getText().toString(),
                PendingIntent.getBroadcast(this, 0, new Intent("ENVIADO"), 0),
                PendingIntent.getBroadcast(this, 0, new Intent("ENTREGUE"), 0)
        );
    }

    private class ReceptorEnviado extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getResultCode() == RESULT_OK) {
                lblEnviado.setText("MENSAGEM ENVIADA");
            } else {
                lblEnviado.setText("ERRO ENVIADO");
            }
        }
    }

    private class ReceptorEntregue extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getResultCode() == RESULT_OK) {
                lblEntregue.setText("MENSAGEM ENTREGUE");
            } else {
                lblEntregue.setText("ERRO ENTREGUE");
            }
        }
    }

    private class ReceptorRecebido extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage[] mensagens = getMensagens(intent);

            StringBuilder sb = new StringBuilder();

            for(SmsMessage sms : mensagens) {
                String telefone = sms.getOriginatingAddress();
                String mensagem = sms.getMessageBody();

                sb.append(telefone + ": " + mensagem + "\n");
            }

            lblRecebido.setText(sb.toString());
        }
    }

    public static SmsMessage[] getMensagens(Intent it) {
        Object[] pdus = (Object[]) it.getSerializableExtra("pdus");

        SmsMessage[] mensagens = new SmsMessage[pdus.length];

        for (int i=0; i<pdus.length; i++) {
            SmsMessage sms = SmsMessage.createFromPdu( (byte[]) pdus[i]);
            mensagens[i] = sms;
        }

        return mensagens;
    }
}
