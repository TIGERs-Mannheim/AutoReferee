/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.game;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.log4j.Logger;


/**
 * Utility Class for Message Signatures
 * This class provides methods to load RSA Keys and to sign and verify arbitrary byte data.
 * The signatures are based on RSA.
 * NOTE: to generate a new key, use the tool in the SSL-Game-Controller repository
 * and process the private key with the script in this module to get a PKCS8-Key-File
 */
public class MessageSigner
{
	private static final Logger log = Logger.getLogger(MessageSigner.class.getName());
	private static final String PRIVATE_KEY_PATH = "/keys/TIGERs-Mannheim.key.pem.pkcs8";
	private static final String PUBLIC_KEY_PATH = "/keys/TIGERs-Mannheim.pub.pem";
	private static final String SIGNING_ALGORITHM = "SHA256WITHRSA";
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	
	/**
	 * Constructor, pass a path to a Private and Public Key
	 * 
	 * @param pathToPrivateKey Path to a private key in PKCS8 Format
	 * @param pathToPublicKey Path to a public key for verification
	 */
	public MessageSigner(URL pathToPrivateKey, URL pathToPublicKey)
	{
		log.debug("Loading private key from: " + pathToPrivateKey);
		log.debug("Loading public key from:" + pathToPublicKey);
		loadKeys(pathToPrivateKey, pathToPublicKey);
	}
	
	
	/**
	 * Default constructor, uses the default keys in the resource directory
	 */
	public MessageSigner()
	{
		log.debug("Loading default keys");
		URL privateKeyPath = getClass().getResource(PRIVATE_KEY_PATH);
		URL publicKeyPath = getClass().getResource(PUBLIC_KEY_PATH);
		loadKeys(privateKeyPath, publicKeyPath);
	}
	
	
	private void loadKeys(URL privateKey, URL publicKey)
	{
		if (privateKey != null)
		{
			try
			{
				this.privateKey = getPrivateKey(Paths.get(privateKey.toURI()).toString());
			} catch (URISyntaxException | FileSystemNotFoundException e)
			{
				log.warn("Can not load private key", e);
				this.privateKey = null;
			}
		} else
		{
			log.info("Could not find private key");
			this.privateKey = null;
		}
		
		if (publicKey != null)
		{
			try
			{
				this.publicKey = getPublicKey(Paths.get(publicKey.toURI()).toString());
			} catch (URISyntaxException e)
			{
				log.warn("Invalid public key path", e);
				this.publicKey = null;
			}
		} else
		{
			log.info("Could not find public key");
			this.publicKey = null;
		}
	}
	
	
	/**
	 * read RSA PKCS8 Private key
	 *
	 * @param path Path to the private key file
	 * @return PrivateKey instance
	 */
	private PrivateKey getPrivateKey(String path)
	{
		try
		{
			byte[] bytes = Files.readAllBytes(Paths.get(path));
			
			// Clean and Decode RSA Key
			String inp = new String(bytes);
			inp = inp.replace("-----BEGIN PRIVATE KEY-----", "");
			inp = inp.replace("-----END PRIVATE KEY-----", "");
			// Remove Whitespace
			inp = inp.replaceAll("\\s+", "");
			byte[] dec = Base64.getDecoder().decode(inp);
			
			// Get Key
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(dec);
			return factory.generatePrivate(spec);
			
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e)
		{
			log.warn("Generating private key failed", e);
		}
		
		return null;
	}
	
	
	/**
	 * load RSA Public Key from File
	 *
	 * @param path Path to RSA Public Key file
	 * @return PublicKey Instance
	 */
	private PublicKey getPublicKey(String path)
	{
		try
		{
			byte[] bytes = Files.readAllBytes(Paths.get(path));
			
			// Clean and Decode RSA Key
			String inp = new String(bytes);
			inp = inp.replace("-----BEGIN PUBLIC KEY-----", "");
			inp = inp.replace("-----END PUBLIC KEY-----", "");
			// Remove Whitespace
			inp = inp.replaceAll("\\s+", "");
			byte[] dec = Base64.getDecoder().decode(inp);
			
			// Get Key
			KeyFactory factory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(dec);
			return factory.generatePublic(spec);
			
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e)
		{
			log.warn("Generating public key failed", e);
		}
		
		return null;
	}
	
	
	/**
	 * Generate Signature
	 *
	 * @param data Data that should be signed
	 * @return The generated signature
	 */
	public byte[] sign(byte[] data)
	{
		if (privateKey == null)
		{
			log.debug("Skipping message signing: no private key");
			return new byte[0];
		}
		
		try
		{
			Signature sig = Signature.getInstance(SIGNING_ALGORITHM);
			sig.initSign(privateKey);
			sig.update(data);
			return sig.sign();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e)
		{
			log.warn("Message signing failed", e);
		}
		
		return new byte[0];
	}
	
	
	/**
	 * Check if a signature is valid
	 *
	 * @param data Data that was signed
	 * @param sig Signature to check
	 * @return True if Signature matches to data
	 */
	public boolean verify(byte[] data, byte[] sig)
	{
		if (publicKey == null)
		{
			log.debug("Skipping message verification: no public key");
			return true;
		}
		try
		{
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sig);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e)
		{
			log.warn("Error while verifying Signature", e);
		}
		return false;
	}
}
