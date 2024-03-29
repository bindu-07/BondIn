package com.binduhait.instagram.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.binduhait.instagram.Model.User;
import com.binduhait.instagram.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantsAd extends RecyclerView.Adapter<AdapterParticipantsAd.MyHolder> {

    Context context;


    ArrayList<User> users;

    public AdapterParticipantsAd(Context context, ArrayList<User> users, String groupid, String mygrouprole) {
        this.context = context;
        this.users = users;
        this.groupid = groupid;
        this.mygrouprole = mygrouprole;
    }

    String groupid,mygrouprole;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_add_participants,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final User modelUsers=users.get(position);
        String name=modelUsers.getFullname();
        String email=modelUsers.getUsername();
        String image=modelUsers.getImageurl();
        final String uid=modelUsers.getId();
        holder.name.setText(name);
        holder.email.setText(email);
        try {
            Glide.with(context).load(image).into(holder.icon);
        }
        catch (Exception e){

        }
        checkIfUserAlreadyExist(modelUsers,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
                reference.child(groupid).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String hisprebviousRole = "" + dataSnapshot.child("role").getValue();
                                    String[] options;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Options");
                                    if (mygrouprole.equals("creator")) {
                                        if (hisprebviousRole.equals("admin")) {
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        removeadmin(modelUsers);
                                                    } else {
                                                        removeParticipants(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisprebviousRole.equals("participants")) {
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        makeadmin(modelUsers);
                                                    } else {
                                                        removeParticipants(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                    } else if (mygrouprole.equals("admin")) {
                                        if (hisprebviousRole.equals("creator")) {
                                            Toast.makeText(context, "Creator of Group", Toast.LENGTH_LONG).show();
                                        } else if (hisprebviousRole.equals("admin")) {
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        removeadmin(modelUsers);
                                                    } else {
                                                        removeParticipants(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        } else if (hisprebviousRole.equals("participants")) {
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        makeadmin(modelUsers);
                                                    } else {
                                                        removeParticipants(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                } else {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                    builder1.setTitle("Add Participants")
                                            .setMessage("Add this User in this Group?")
                                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addParticipants(modelUsers);
                                                }
                                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void makeadmin(User modelUsers) {

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("role","admin");
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupid).child("Participants").child(modelUsers.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"The User is now Admin",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addParticipants(User modelUsers) {
        String timestamp=""+System.currentTimeMillis();
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("id",modelUsers.getId());
        hashMap.put("role","participants");
        hashMap.put("timestamp",timestamp);
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupid).child("Participants").child(modelUsers.getId()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"added Sucessfully",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    private void removeParticipants(User modelUsers) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupid).child("Participants").child(modelUsers.getId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"Removed Sucessfully",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    private void removeadmin(User modelUsers){
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("role","participants");
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupid).child("Participants").child(modelUsers.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"The User is no longer admin",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void checkIfUserAlreadyExist(User modelUsers, final MyHolder holder){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupid).child("Participants").child(modelUsers.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String hisRole=""+dataSnapshot.child("role").getValue();
                            holder.status.setText(hisRole);
                        }
                        else {
                            holder.status.setVisibility(View.INVISIBLE);
                            holder.status.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView name,email,status;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.imagepg);
            name=itemView.findViewById(R.id.namepg);
            email=itemView.findViewById(R.id.emailpg);
            status=itemView.findViewById(R.id.status);
        }
    }
}