package ichack18.emotionpicker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class StartActivity extends AppCompatActivity {

    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        try {
            socket = IO.socket(MainActivity.SERVER_IP);
            socket.on("location", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Intent i = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
        } catch (Exception e) {

        }

        Button button = (Button) findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) findViewById(R.id.location_text);
                if (text.getText().toString() != null && text.getText().toString().length() > 0) {
                    if (socket != null) {
                        socket.emit("location_info", null);
                    }
                }
            }
        });
    }
}
