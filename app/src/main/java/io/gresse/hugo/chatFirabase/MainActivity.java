package io.gresse.hugo.chatFirabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ValueEventListener, MessageAdapter.Listener {

    EditText       mInputEditText;
    ImageButton    mSendButton;
    MessageAdapter mMessageAdapter;
    RecyclerView mRecyclerView;

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UserStorage.isUserLoggedIn(this)) {
            Intent intent = new Intent(this, NamePickerActivity.class);
            this.startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerView);
        mInputEditText = findViewById(R.id.inputEditText);
        mSendButton = findViewById(R.id.sendButton);

        User user=UserStorage.getUserInfo(this);

        mMessageAdapter = new MessageAdapter(this, new ArrayList<Message>(), user);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);

        connectAndListenToFirebase();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitNewMessage(mInputEditText.getText().toString());
                mInputEditText.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            UserStorage.disconnectUser(this);
            Intent intent = new Intent(this, NamePickerActivity.class);
            this.startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseReference.removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        List<Message> items = new ArrayList<>();
        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
            Message message = messageSnapshot.getValue(Message.class);
            message.key = messageSnapshot.getKey();
            items.add(message);
        }
        mMessageAdapter.setData(items);
        mRecyclerView.scrollToPosition(items.size()-1);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(this, "Error: " + databaseError, Toast.LENGTH_SHORT).show();

    }

    private void connectAndListenToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference(Constant.FIREBASE_PATH);

        mDatabaseReference.addValueEventListener(this);
    }

    private void submitNewMessage(String message) {
        User user = UserStorage.getUserInfo(this);
        if (message.isEmpty() || user == null) {
            return;
        }
        DatabaseReference newData  = mDatabaseReference.push();
        newData.setValue(
                new Message(message,
                        user.name,
                        user.email,
                        System.currentTimeMillis()));
    }

    @Override
    public void onItemClick(int position, Message message) {
        mDatabaseReference.child(message.key).removeValue();
    }

    @Override
    public void onItemLongClick(int position, final Message message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");
        builder.setItems(R.array.arrayOptionLongClick, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        mDatabaseReference.child(message.key).removeValue();
                        break;
                    case 1:
                        mDatabaseReference.child(message.key).setValue(new Message("Message modifié",message.userName,message.userEmail,message.timestamp));
                }
            }
        });
        builder.show();
    }
}
