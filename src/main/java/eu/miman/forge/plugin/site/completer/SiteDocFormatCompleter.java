package eu.miman.forge.plugin.site.completer;

import java.util.Arrays;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;

/**
 * @author Mikael Thorman
 *
 */
public class SiteDocFormatCompleter extends SimpleTokenCompleter
{

   /* (non-Javadoc)
    * @see org.jboss.forge.shell.completer.SimpleTokenCompleter#getCompletionTokens()
    */
   @Override
   public Iterable<?> getCompletionTokens()
   {
	   return Arrays.asList(SiteDocFormatType.APT, SiteDocFormatType.MARKDOWN);
   }

}
