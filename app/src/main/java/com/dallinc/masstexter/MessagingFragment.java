package com.dallinc.masstexter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dallinc.masstexter.messaging.Compose;
import com.dallinc.masstexter.models.GroupMessage;
import com.dallinc.masstexter.models.Template;
import com.dallinc.masstexter.templates.EditTemplate;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Created by dallin on 1/30/15.
 */
public class MessagingFragment extends Fragment {
    List<GroupMessage> sentMessages = GroupMessage.listAll(GroupMessage.class);
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MessagingFragment newInstance(int sectionNumber) {
        MessagingFragment fragment = new MessagingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MessagingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.messaging_fragment, container, false);

        final FloatingActionsMenu composeButton = (FloatingActionsMenu) rootView.findViewById(R.id.buttonComposeMessage);
        FloatingActionButton usingTemplateButton = (FloatingActionButton) rootView.findViewById(R.id.buttonComposeUsingTemplate);
        usingTemplateButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                composeButton.collapse();
                final AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                builder.setTitle("Select Template");
                List<Template> _templates = Template.listAll(Template.class);
                if(_templates.size() < 1) {
                    Toast.makeText(rootView.getContext(), "You do not have any templates saved!", Toast.LENGTH_LONG).show();
                    return;
                }
                final Template[] templates = _templates.toArray(new Template[_templates.size()]);
                final String[] template_titles = new String[templates.length];
                for(int i=0; i<templates.length; i++) {
                    template_titles[i] = templates[i].title;
                }
                builder.setItems(template_titles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(rootView.getContext(), Compose.class);
                        intent.putExtra("template_id", templates[which].getId());
                        startActivity(intent);
                    }
                });
                builder.create().show();
            }
        });

        FloatingActionButton quickComposeButton = (FloatingActionButton) rootView.findViewById(R.id.buttonQuickCompose);
        quickComposeButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                composeButton.collapse();
                Intent intent = new Intent(rootView.getContext(), Compose.class);
                startActivity(intent);
            }
        });

        RecyclerView recList = (RecyclerView) rootView.findViewById(R.id.sentMessagesCardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        GroupMessageAdapter ca = new GroupMessageAdapter(sentMessages);
        recList.setAdapter(ca);

        return rootView;
    }

    public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.GroupMessageViewHolder> {

        private List<GroupMessage> sentMessageList;

        public GroupMessageAdapter(List<GroupMessage> sentMessageList) {
            this.sentMessageList = sentMessageList;
        }

        @Override
        public int getItemCount() {
            return sentMessageList.size();
        }

        @Override
        public void onBindViewHolder(final GroupMessageViewHolder GroupMessageViewHolder, int i) {
            final GroupMessage sentMessage = sentMessageList.get(i);
            GroupMessageViewHolder.vTitle.setText(sentMessage.sentAt);
            String body = sentMessage.messageBody;
            sentMessage.buildArrayListFromString();
            for(String variable: sentMessage.variables) {
                body = body.replaceFirst("¬", variable);
            }
            GroupMessageViewHolder.vBody.setText(body);
            GroupMessageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: navigate to detail view
//                    Intent intent = new Intent(GroupMessageViewHolder.itemView.getContext(), SentMessageDetails.class);
//                    intent.putExtra("message_id", sentMessage.getId());
//                    startActivity(intent);
                }
            });
            GroupMessageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessageViewHolder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setMessage("Do you want to delete this message from the list?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sentMessage.delete();
                            sentMessageList = sentMessages = GroupMessage.listAll(GroupMessage.class);
                            notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        }

        @Override
        public GroupMessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_card_layout, viewGroup, false);
            return new GroupMessageViewHolder(itemView);
        }

        public class GroupMessageViewHolder extends RecyclerView.ViewHolder {
            protected TextView vTitle;
            protected TextView vBody;

            public GroupMessageViewHolder(View v) {
                super(v);
                vTitle =  (TextView) v.findViewById(R.id.templateCardTitle);
                vBody = (TextView)  v.findViewById(R.id.templateCardBody);
            }
        }
    }
}
