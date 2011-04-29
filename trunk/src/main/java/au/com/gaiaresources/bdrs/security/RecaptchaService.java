package au.com.gaiaresources.bdrs.security;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

public class RecaptchaService {
    private Map<String, ReCaptcha> recaptchas;
    private String privateKey;
    private String publicKey;
    
    private static final String PUBLIC_KEY_PROPERTY = "recaptcha.public.key";
    private static final String PRIVATE_KEY_PROPERTY = "recaptcha.private.key";
    
    private static RecaptchaService INSTANCE;
    
    public static RecaptchaService create() throws Exception {
        INSTANCE = new RecaptchaService();
        return INSTANCE;
    }
    
    public static RecaptchaService getInstance() {
        return INSTANCE;
    }
    
    private RecaptchaService() throws Exception {
        recaptchas = new HashMap<String, ReCaptcha>();
        Properties p = new Properties();
        p.load(getClass().getResourceAsStream("recaptcha.properties"));
        if (!p.containsKey(PUBLIC_KEY_PROPERTY) || !p.containsKey(PRIVATE_KEY_PROPERTY)) {
            throw new IllegalArgumentException("recaptcha.properties must contain " + PUBLIC_KEY_PROPERTY
                                             + " and " + PRIVATE_KEY_PROPERTY);
        }
        this.publicKey = p.getProperty(PUBLIC_KEY_PROPERTY);
        this.privateKey = p.getProperty(PRIVATE_KEY_PROPERTY);
    }
    
    public void start(String id) {
        ReCaptcha r = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
        recaptchas.put(id, r);
    }
    
    public void render(String id, OutputStream out) throws IOException {
        out.write(getContent(id).getBytes());
    }
    
    public void render(String id, Writer writer) throws IOException {
        writer.write(getContent(id));
    }
    
    private String getContent(String id) {
        return recaptchas.get(id).createRecaptchaHtml(null, null);
    }
    
    public boolean validate(String id, String remoteAddress, String challenge, String response) {
        ReCaptcha r = recaptchas.get(id);
        ReCaptchaResponse recaptchaResponse = r.checkAnswer(remoteAddress, challenge, response);
        recaptchas.remove(id);
        return recaptchaResponse.isValid();
    }
}
