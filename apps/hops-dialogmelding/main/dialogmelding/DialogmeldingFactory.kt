package dialogmelding

import no.nav.helse.hops.domain.PersonId
import no.nav.helse.hops.security.toIsoString

object DialogmeldingFactory {
    /**
     * Notat kan blant annet benyttes for å sende opplysninger relatert til en
     * pasientbehandling hvor det ikke forventes at det skal sendes noe svar tilbake fra
     * mottaker, i tilfeller som ikke dekkes av en annen innholdsstandard
     */
    fun createNoteFromNavToPractitioner(m: DialogNotatMeta) =
        """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <ff:EI_fellesformat xmlns:ff="http://www.nav.no/xml/eiff/2/">
                <MsgHead xmlns="http://www.kith.no/xmlstds/msghead/2006-05-24">
                    <MsgInfo>
                        <Type V="DIALOG_NOTAT" DN="Notat" />
                        <MIGversion>v1.2 2006-05-24</MIGversion>
                        <GenDate>${m.timestamp.toIsoString()}</GenDate>
                        <MsgId>${m.id}</MsgId>
                        <Ack V="J" DN="Ja" />
                        <Sender>
                            <Organisation>
                                <OrganisationName>NAV</OrganisationName>
                                <Ident>
                                    <Id>889640782</Id>
                                    <TypeId V="ENH" S="2.16.578.1.12.4.1.1.9051" DN="Organisasjonsnummeret i Enhetsregisteret" />
                                </Ident>
                                <Ident>
                                    <Id>79768</Id>
                                    <TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="Identifikator fra Helsetjenesteenhetsregisteret (HER-id)" />
                                </Ident>
                            </Organisation>
                        </Sender>
                        <Receiver>
                            <Organisation>
                                <OrganisationName>${m.mottaker.navn}</OrganisationName>
                                <Ident>
                                    <Id>${m.mottaker.herId}</Id>
                                    <TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="Identifikator fra Helsetjenesteenhetsregisteret (HER-id)" />
                                </Ident>
                                <HealthcareProfessional>
                                    <Ident>
                                        <Id>${m.behandler.hprId}</Id>
                                        <TypeId V="HPR" S="2.16.578.1.12.4.1.1.8116" DN="HPR-nummer" />
                                    </Ident>
                                </HealthcareProfessional>
                            </Organisation>
                        </Receiver>
                        <Patient>
                            <Ident>
                                <Id>${m.pasient.pid}</Id>
                                ${if (m.pasient.pid.type == PersonId.Type.FNR)
                                    """<TypeId V="FNR" S="2.16.578.1.12.4.1.1.8116" DN="Fødselsnummer" />"""
                                else
                                    """<TypeId V="DNR" S="2.16.578.1.12.4.1.1.8116" DN="D-nummer" />"""
                                }
                            </Ident>
                        </Patient>
                    </MsgInfo>
                    <Document>
                        <DocumentConnection V="H" DN="Hoveddokument" />
                        <RefDoc>
                            <MsgType V="XML" DN="XML-instans" />
                            <MimeType>text/xml</MimeType>
                            <Content>
                                <Dialogmelding xmlns="http://www.kith.no/xmlstds/dialog/2006-10-11">
                                    <Notat>
                                        <TemaKodet V="1" S="2.16.578.1.12.4.1.1.7321" DN="Notat om pasient" />
                                        <TekstNotatInnhold xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema">Åpne PDF-vedlegg</TekstNotatInnhold>
                                    </Notat>
                                </Dialogmelding>
                            </Content>
                        </RefDoc>
                    </Document>
                    <Document>
                        <DocumentConnection V="V" DN="Vedlegg" />
                        <RefDoc>
                            <MsgType V="A" DN="Vedlegg" />
                            <MimeType>application/pdf</MimeType>
                            <Content>
                                <Base64Container xmlns="http://www.kith.no/xmlstds/base64container">${java.util.Base64.getEncoder().encodeToString(m.pdf)}</Base64Container>
                            </Content>
                        </RefDoc>
                    </Document>
                </MsgHead>
            </ff:EI_fellesformat>
        """.trimIndent()
}
