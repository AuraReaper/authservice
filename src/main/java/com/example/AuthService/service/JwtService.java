package com.example.AuthService.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String SECRET_KEY = "OWU4MjFmYTRkMTg4YzMwMjhjY2JhYzBiMTNhZmU5NTU5ZGU0ZDY2N2EyYjhiYzU5ZjRhM2NmZDI0ODY2YmQwZg==";

   public String extractUsername(String token){
       return extractClaim(token, Claims::getSubject);
   }

   public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
       final Claims claims = extractAllClaims(token);
       return claimsResolver.apply(claims);
   }

   public Date extractExpiration(String token){
       return extractClaim(token, Claims::getExpiration);
   }

   public Boolean isTokenExpired(String token){
       return extractExpiration(token).before(new Date());
   }

   public Boolean validateToken(String token, UserDetails userDetails){
       final String username = extractUsername(token);
       return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
   }

   public String createToken(Map<String, Object> claims, String username){
       return Jwts.builder()
               .setClaims(claims)
               .setSubject(username)
               .setIssuedAt(new Date(System.currentTimeMillis()))
               .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
               .signWith(getSignKey(), SignatureAlgorithm.HS256)
               .compact();
   }

   private Claims extractAllClaims(String token){
       return Jwts
               .parser()
               .setSigningKey(getSignKey())
               .build()
               .parseClaimsJws(token)
               .getBody();
   }

   private Key getSignKey(){
       byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
       return Keys.hmacShaKeyFor(keyBytes);
   }

   public String GenerateToken(String username){
       return createToken(Map.of(), username);
   }
}
