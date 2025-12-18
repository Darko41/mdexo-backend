package com.doublez.backend.service.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.doublez.backend.config.EmailProperties;
import com.doublez.backend.dto.email.EmailDTO;
import com.doublez.backend.entity.user.UserTier;

@Component
public class EmailTemplateService {

	private final EmailProperties emailProperties;

	public EmailTemplateService(EmailProperties emailProperties) {
		this.emailProperties = emailProperties;
	}

	// Verification submitted template
	public EmailDTO createVerificationSubmittedEmail(String userEmail, String userName, String documentType) {
		String subject = "Verifikacija podnesena - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Uspešno ste podneli zahtev za verifikaciju (%s).
				Vaša prijava će biti pregledana u najkraćem mogućem roku.

				Hvala Vam na strpljenju.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, documentType, emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}

	// Verification approved template
	public EmailDTO createVerificationApprovedEmail(String userEmail, String userName, String role) {
		String subject = "Verifikacija odobrena - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Vaša verifikacija je uspešno odobrena!
				Sada imate pristup svim funkcijama %s na našoj platformi.

				Hvala Vam što koristite našu platformu.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, getRoleDisplayName(role), emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}

	// Verification rejected template
	public EmailDTO createVerificationRejectedEmail(String userEmail, String userName, String rejectionReason) {
		String subject = "Verifikacija odbijena - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Nažalost, vaša verifikacija je odbijena.

				Razlog: %s

				Molimo Vas da ispravite navedene probleme i ponovo pošaljete zahtev.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, rejectionReason, emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}

	// License expiring soon template
	public EmailDTO createLicenseExpiringEmail(String userEmail, String userName, LocalDate expiryDate) {
		String subject = "Obaveštenje o isteku licence - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Vaša licenca će isteći %s.
				Molimo Vas da obnovite licencu kako biste nastavili da koristite sve funkcije platforme.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, expiryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")),
				emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}

	// New verification request for admins
	public EmailDTO createNewVerificationAdminEmail(String userName, String documentType) {
		String subject = "Novi zahtev za verifikaciju - Real Estate Platform";
		String body = """
				Postoji novi zahtev za verifikaciju koji zahteva pregled.

				Korisnik: %s
				Tip dokumenta: %s

				Molimo Vas da pregledate zahtev u admin panelu.

				S poštovanjem,
				Real Estate Platform
				""".formatted(userName, documentType);

		return new EmailDTO(emailProperties.getAdminEmail(), subject, body);
	}

	private String getRoleDisplayName(String role) {
		switch (role) {
		case "ROLE_AGENT":
			return "Agenta";
		case "ROLE_INVESTOR":
			return "Investitora";
		case "ROLE_AGENCY_ADMIN":
			return "Administratora Agencije";
		default:
			return "Korisnika";
		}
	}

	// In EmailTemplateService.java
	public EmailDTO createTrialStartedEmail(String userEmail, String userName, int trialMonths) {
		String subject = "Dobrodošli - Besplatni probni period od " + trialMonths + " meseci";
		String body = """
				Poštovani/poštovana %s,

				Dobrodošli na Real Estate Platform!

				Aktiviran Vam je besplatni probni period od %d meseci koji ističe %s.

				Tokom probnog perioda možete:
				• Kreirati do 3 nekretnine
				• Dodati do 20 slika
				• Koristiti sve osnovne funkcije platforme

				Nakon isteka probnog perioda, bićete automatski prebačeni na osnovni paket.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, trialMonths,
				LocalDate.now().plusMonths(trialMonths).format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")),
				emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}

	public EmailDTO createTrialExpiringEmail(String userEmail, String userName, int daysRemaining) {
		String subject = "Probni period ističe za " + daysRemaining + " dan/a - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Vaš probni period ističe za %d dan/a.

				Nakon isteka, bićete automatski prebačeni na osnovni paket. Ako želite nastaviti sa svim funkcijama,
				molimo Vas da odaberete odgovarajući paket u svom profilu.

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, daysRemaining, emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}

//	public EmailDTO createTrialExpiredEmail(String userEmail, String userName, UserTier newTier) {
//		String subject = "Probni period istekao - Real Estate Platform";
//		String body = """
//				Poštovani/poštovana %s,
//
//				Vaš probni period je istekao.
//
//				Sada ste prebačeni na %s paket. Molimo Vas da nadogradite paket ukoliko želite pristup svim funkcijama.
//
//				S poštovanjem,
//				Real Estate Platform Team
//				%s
//				""".formatted(userName, getTierDisplayName(newTier), emailProperties.getSupportEmail());
//
//		return new EmailDTO(userEmail, subject, body);
//	}

	public EmailDTO createTrialExtendedEmail(String userEmail, String userName, int additionalMonths) {
		String subject = "Probni period produžen - Real Estate Platform";
		String body = """
				Poštovani/poštovana %s,

				Vaš probni period je produžen za %d meseci.

				Sada ističe %s.

				Hvala Vam što koristite našu platformu!

				S poštovanjem,
				Real Estate Platform Team
				%s
				""".formatted(userName, additionalMonths,
				LocalDate.now().plusMonths(additionalMonths).format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")),
				emailProperties.getSupportEmail());

		return new EmailDTO(userEmail, subject, body);
	}
//
//	private String getTierDisplayName(UserTier tier) {
//	    if (tier == null) return "Osnovni";
//	    
//	    switch (tier) {
//	        case FREE_USER: return "Besplatni korisnički";
//	        case BASIC_USER: return "Osnovni korisnički";
//	        case PREMIUM_USER: return "Premium korisnički";
//	        case AGENCY_BASIC: return "Osnovni agencijski";
//	        case AGENCY_PREMIUM: return "Premium agencijski";
//	        case FREE_INVESTOR: return "Besplatni investitorski";
//	        case BASIC_INVESTOR: return "Osnovni investitorski";
//	        case PREMIUM_INVESTOR: return "Premium investitorski";
//	        case ADMIN: return "Administrator";
//	        default: return "Osnovni";
//	    }
//	}
}
