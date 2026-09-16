package com.notif.delivery.service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.notif.common.domain.delivery.DeliveryPurpose;
import com.notif.common.domain.delivery.DeliveryStatus;
import com.notif.common.dto.delivery.EnqueueDeliveryCommand;
import com.notif.common.entity.delivery.DeliveryJob;
import com.notif.common.port.DeliveryDispatcher;
import com.notif.delivery.repository.DeliveryJobRepository;

@Service
public class DeliveryService implements DeliveryDispatcher {

    private final DeliveryJobRepository jobs;
    private final TemplateEngine templateEngine;

    public DeliveryService(DeliveryJobRepository jobs, TemplateEngine templateEngine) {
        this.jobs = jobs;
        this.templateEngine = templateEngine;
    }

    @Override
    @Transactional
    public UUID enqueue(EnqueueDeliveryCommand command) {
        DeliveryJob job = new DeliveryJob();
        job.setId(UUID.randomUUID());
        job.setPurpose(command.purpose());
        job.setChannel(command.channel());
        job.setRecipient(command.recipient());
        job.setLocale(command.locale() == null || command.locale().isBlank() ? "hu" : command.locale());
        job.setPayload(command.payload());
        job.setStatus(DeliveryStatus.queued);
        job.setAttemptCount(0);
        job.setCreatedAt(Instant.now());
        applyTemplate(job, command);
        return jobs.save(job).getId();
    }

    private void applyTemplate(DeliveryJob job, EnqueueDeliveryCommand command) {
        if (command.subject() != null || command.bodyText() != null || command.bodyHtml() != null) {
            job.setSubject(command.subject());
            job.setBodyText(command.bodyText());
            job.setBodyHtml(command.bodyHtml());
            if (job.getPurpose() == DeliveryPurpose.INVITE && job.getBodyHtml() == null) {
                renderInvite(job);
            }
            return;
        }
        if (job.getPurpose() == DeliveryPurpose.INVITE) {
            renderInvite(job);
            return;
        }
        if (job.getPurpose() == DeliveryPurpose.TEST) {
            renderTest(job);
            return;
        }
        if (job.getPurpose() == DeliveryPurpose.ALERT) {
            renderAlert(job);
        }
    }

    private void renderAlert(DeliveryJob job) {
        Map<String, Object> payload = job.getPayload() == null ? Map.of() : job.getPayload();
        boolean en = "en".equalsIgnoreCase(job.getLocale());
        String headline = String.valueOf(payload.getOrDefault("headline", en ? "Notif alert" : "Notif riasztás"));
        String reason = String.valueOf(payload.getOrDefault("reason", ""));
        String url = String.valueOf(payload.getOrDefault("url", ""));
        String level = String.valueOf(payload.getOrDefault("level", ""));
        Context ctx = new Context(en ? Locale.ENGLISH : Locale.forLanguageTag("hu"));
        ctx.setVariable("heading", headline);
        ctx.setVariable("body", reason);
        ctx.setVariable("url", url);
        ctx.setVariable("level", level);
        ctx.setVariable("channel", job.getChannel().name());
        job.setSubject(headline);
        job.setBodyText((reason + (url.isBlank() ? "" : " " + url)).trim());
        job.setBodyHtml(templateEngine.process("alert-email", ctx));
    }

    private void renderInvite(DeliveryJob job) {
        Map<String, Object> payload = job.getPayload() == null ? Map.of() : job.getPayload();
        String link = String.valueOf(payload.getOrDefault("inviteUrl", ""));
        String inviter = String.valueOf(payload.getOrDefault("inviterName", "Notif"));
        boolean en = "en".equalsIgnoreCase(job.getLocale());
        Context ctx = new Context(en ? Locale.ENGLISH : Locale.forLanguageTag("hu"));
        ctx.setVariable("inviteUrl", link);
        ctx.setVariable("inviterName", inviter);
        ctx.setVariable("heading", en ? "You're invited" : "Meghívó");
        ctx.setVariable("body", en
                ? inviter + " invited you to Notif. There is no public registration — only this link."
                : inviter + " meghívott a Notifra. Nincs nyilvános regisztráció — csak ez a link.");
        ctx.setVariable("cta", en ? "Accept invite" : "Elfogadom a meghívást");
        ctx.setVariable("footnote", en ? "This link expires in 7 days." : "A link 7 napig él.");
        job.setSubject(en ? "You're invited to Notif" : "Meghívó a Notifra");
        job.setBodyText(en
                ? inviter + " invited you to Notif. Open: " + link
                : inviter + " meghívott a Notifra. Nyisd meg: " + link);
        job.setBodyHtml(templateEngine.process("invite-email", ctx));
    }

    private void renderTest(DeliveryJob job) {
        boolean en = "en".equalsIgnoreCase(job.getLocale());
        Context ctx = new Context(en ? Locale.ENGLISH : Locale.forLanguageTag("hu"));
        ctx.setVariable("channel", job.getChannel().name());
        ctx.setVariable("heading", en ? "Notif test" : "Notif teszt");
        ctx.setVariable("body", en
                ? "Notif is alive on this channel: " + job.getChannel()
                : "Notif él ezen a csatornán: " + job.getChannel());
        job.setSubject(en ? "Notif test" : "Notif teszt");
        job.setBodyText(en
                ? "Notif is alive on this channel: " + job.getChannel()
                : "Notif él ezen a csatornán: " + job.getChannel());
        job.setBodyHtml(templateEngine.process("test-email", ctx));
    }

    public java.util.List<DeliveryJob> listRecent() {
        return jobs.findTop100ByOrderByCreatedAtDesc();
    }

    public java.util.Optional<DeliveryJob> latestInvite(String email) {
        return jobs.findFirstByPurposeAndRecipientOrderByCreatedAtDesc(DeliveryPurpose.INVITE, email);
    }
}
