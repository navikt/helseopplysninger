package dialogmelding

object DialogmeldingFactory {
    fun create(m: Melding): String {
        return """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <EI_fellesformat xmlns="http://www.nav.no/xml/eiff/2/" xmlns:ns6="http://www.kith.no/xmlstds/base64container" xmlns:ns5="http://www.kith.no/xmlstds/felleskomponent1" xmlns:ns2="http://www.kith.no/xmlstds/msghead/2006-05-24" xmlns:ns4="http://www.kith.no/xmlstds/dialog/2006-10-11" xmlns:ns3="http://www.w3.org/2000/09/xmldsig#">
                <ns2:MsgHead>
                    <ns2:MsgInfo>
                        <ns2:Type V="DIALOG_NOTAT" DN="Notat"/>
                        <ns2:MIGversion>v1.2 2006-05-24</ns2:MIGversion>
                        <ns2:GenDate>2021-10-28T14:41:18.5832926</ns2:GenDate>
                        <ns2:MsgId>ce6be2f4-18dc-4371-a52b-d7536f9b5150</ns2:MsgId>
                        <ns2:Ack V="J" DN="Ja"/>
                        <ns2:Sender>
                            <ns2:Organisation>
                                <ns2:OrganisationName>NAV</ns2:OrganisationName>
                                <ns2:Ident>
                                    <ns2:Id>889640782</ns2:Id>
                                    <ns2:TypeId V="ENH" S="2.16.578.1.12.4.1.1.9051" DN="Organisasjonsnummeret i Enhetsregisteret"/>
                                </ns2:Ident>
                                <ns2:Ident>
                                    <ns2:Id>79768</ns2:Id>
                                    <ns2:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"/>
                                </ns2:Ident>
                            </ns2:Organisation>
                        </ns2:Sender>
                        <ns2:Receiver>
                            <ns2:Organisation>
                                <ns2:OrganisationName>${m.mottaker.navn}</ns2:OrganisationName>
                                <ns2:Ident>
                                    <ns2:Id>${m.mottaker.herId}</ns2:Id>
                                    <ns2:TypeId V="HER" S="2.16.578.1.12.4.1.1.9051" DN="Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"/>
                                </ns2:Ident>
                                <ns2:Ident>
                                    <ns2:Id>${m.mottaker.orgnummer}</ns2:Id>
                                    <ns2:TypeId V="ENH" S="2.16.578.1.12.4.1.1.9051" DN="Organisasjonsnummeret i Enhetsregisteret"/>
                                </ns2:Ident>
                                <ns2:Address>
                                    <ns2:Type V="RES" DN="Besøksadresse"/>
                                    <ns2:StreetAdr>${m.mottaker.adresse}</ns2:StreetAdr>
                                    <ns2:PostalCode>${m.mottaker.postnummer}</ns2:PostalCode>
                                    <ns2:City>${m.mottaker.poststed}</ns2:City>
                                </ns2:Address>
                                <ns2:HealthcareProfessional>
                                    <ns2:RoleToPatient V="6" S="2.16.578.1.12.4.1.1.9034" DN="Fastlege"/>
                                    <ns2:FamilyName>${m.behandler.etternavn}</ns2:FamilyName>
                                    <ns2:MiddleName>${m.behandler.mellomnavn}</ns2:MiddleName>
                                    <ns2:GivenName>${m.behandler.fornavn}</ns2:GivenName>
                                    <ns2:Ident>
                                        <ns2:Id>${m.behandler.fnr}</ns2:Id>
                                        <ns2:TypeId V="FNR" S="2.16.578.1.12.4.1.1.8116" DN="Fødselsnummer Norsk fødselsnummer"/>
                                    </ns2:Ident>
                                    <ns2:Ident>
                                        <ns2:Id>${m.behandler.hprId}</ns2:Id>
                                        <ns2:TypeId V="HPR" S="2.16.578.1.12.4.1.1.8116" DN="HPR-nummer"/>
                                    </ns2:Ident>
                                </ns2:HealthcareProfessional>
                            </ns2:Organisation>
                        </ns2:Receiver>
                        <ns2:Patient>
                            <ns2:FamilyName>${m.pasient.etternavn}</ns2:FamilyName>
                            <ns2:MiddleName>${m.pasient.mellomnavn}</ns2:MiddleName>
                            <ns2:GivenName>${m.pasient.fornavn}</ns2:GivenName>
                            <ns2:Ident>
                                <ns2:Id>${m.pasient.fnr}</ns2:Id>
                                <ns2:TypeId V="FNR" S="2.16.578.1.12.4.1.1.8116" DN="Fødselsnummer"/>
                            </ns2:Ident>
                        </ns2:Patient>
                    </ns2:MsgInfo>
                    <ns2:Document>
                        <ns2:DocumentConnection V="H" DN="Hoveddokument"/>
                        <ns2:RefDoc>
                            <ns2:IssueDate V="2021-10-28"/>
                            <ns2:MsgType V="XML" DN="XML-instans"/>
                            <ns2:MimeType>text/xml</ns2:MimeType>
                            <ns2:Content>
                                <ns4:Dialogmelding>
                                    <ns4:Notat>
                                        <ns4:TemaKodet V="1" S="2.16.578.1.12.4.1.1.8127" DN="Oppfølgingsplan"/>
                                        <ns4:TekstNotatInnhold xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">Åpne PDF-vedlegg</ns4:TekstNotatInnhold>
                                        <ns4:DokIdNotat>edf0f3f6-b6fc-4d23-8496-6864df8de751</ns4:DokIdNotat>
                                        <ns4:RollerRelatertNotat>
                                            <ns4:RolleNotat V="1" S="2.16.578.1.12.4.1.1.9057"/>
                                            <ns4:Person/>
                                        </ns4:RollerRelatertNotat>
                                    </ns4:Notat>
                                </ns4:Dialogmelding>
                            </ns2:Content>
                        </ns2:RefDoc>
                    </ns2:Document>
                    <ns2:Document>
                        <ns2:DocumentConnection V="V" DN="Vedlegg"/>
                        <ns2:RefDoc>
                            <ns2:IssueDate V="2021-10-28"/>
                            <ns2:MsgType V="A" DN="Vedlegg"/>
                            <ns2:MimeType>application/pdf</ns2:MimeType>
                            <ns2:Content>
                                <ns6:Base64Container>${java.util.Base64.getEncoder().encodeToString(m.vedlegg)}</ns6:Base64Container>
                            </ns2:Content>
                        </ns2:RefDoc>
                    </ns2:Document>
                </ns2:MsgHead>
                <MottakenhetBlokk partnerReferanse="herId" ebRole="Saksbehandler" ebService="Oppfolgingsplan" ebAction="Plan"/>
            </EI_fellesformat>
        """.trimIndent()
    }
}