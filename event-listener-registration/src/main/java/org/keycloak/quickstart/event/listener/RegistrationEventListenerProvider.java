package org.keycloak.quickstart.event.listener;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.*;

public class RegistrationEventListenerProvider implements EventListenerProvider {
    private KeycloakSession session;

    private Set<EventType> excludedEvents;
    private Set<OperationType> excludedAdminOperations;

    public RegistrationEventListenerProvider(Set<EventType> excludedEvents, Set<OperationType> excludedAdminOperations, KeycloakSession session) {
        this.session = session;
        this.excludedEvents = excludedEvents;
        this.excludedAdminOperations = excludedAdminOperations;
    }

    @Override
    public void onEvent(Event event) {

        // ignore events othen then Register
        if(event.getType()!= EventType.REGISTER){
            return;
        }

        System.out.println("Event type is : " + event.getType().name());

        // getting realm model from the session
        RealmModel realmModel = session.realms().getRealm(event.getRealmId());

        // user model to get user details
        UserModel user=session.users().getUserById(event.getUserId(), realmModel);

        try{
            EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
            List<Object> subjecAttr = new ArrayList<>();
            subjecAttr.add(0, realmModel.getName());
            subjecAttr.add(1, user.getFirstName());

            Map<String, Object> bodyAttr = new HashMap<>();
            bodyAttr.put("realmName", realmModel.getName());
            bodyAttr.put("user",user.getFirstName());

            // this will make sure that user need to verify the email.
            user.addRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL);
            user.addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP);

            emailTemplateProvider
                    .setRealm(realmModel)
                    .setUser(user)
                    .setAttribute("realmName", realmModel.getDisplayName())
                    .setAttribute("user",user.getFirstName())
                    .send("welcomeEmailHTMLSubject",subjecAttr,"welcome-mail.ftl",bodyAttr);
        }catch (EmailException e){
            System.out.println("Error occured while sending mail. "+e.getMessage());
        }

        if (excludedEvents != null && excludedEvents.contains(event.getType())) {
            return;
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
    // Ignore excluded operations
        if (excludedAdminOperations != null && excludedAdminOperations.contains(adminEvent.getOperationType())) {
            return;
        }
    }

    @Override
    public void close() {

    }
}
