package net.ldcc.playground.util;

import net.ldcc.playground.model.MemberSec;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface OAuthTokenProvider {

    public String createToken(String code, String state, String redirectUri);

    public MemberSec getSubject(String token) throws GeneralSecurityException, IOException;
}
