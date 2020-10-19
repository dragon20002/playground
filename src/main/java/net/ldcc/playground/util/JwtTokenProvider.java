package net.ldcc.playground.util;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
	
	private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private final String issuer;
	private final long exprTime; //millisecond

	public JwtTokenProvider(@Value("${security.jwt.issuer}") String issuer,
			@Value("${security.jwt.exprTime}") long exprTime) {
		this.issuer = issuer;
		this.exprTime = exprTime;
	}

	public String createToken(String subject) {
		Date issue = new Date(); //발행일시
		Date expr = new Date(); //만료일시
		expr.setTime(issue.getTime() + exprTime);

		return Jwts.builder()
//				.setId(id)
				.setIssuer(issuer)
				.setIssuedAt(issue)
				.setExpiration(expr)
				.setSubject(subject)
				.signWith(key).compact();
	}

	public String getSubject(String jws) {
		Claims jwt = null;
		try {
			jwt	= Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(jws).getBody();
		} catch(Exception e) {
			logger.debug("Fail to validation of jws={}", jws);
		}

		Date now = new Date();
		if (jwt == null ||
				!jwt.getIssuer().contentEquals(issuer) ||
				!jwt.getExpiration().after(now) ||
				!jwt.getIssuedAt().before(now)) {
			return null;
		}

		return jwt.getSubject();
	}

}
