#include <config.h>

/**
 * 去掉字符串两头空格
 */
char *trim(char *s)
{
  int i = 0;
  int j = strlen (s) - 1;
  int k = 0;
  while (isspace(s[i]) && s[i] != '\0')
    i++;
  while (isspace(s[j]) && j >= 0)
    j--;
  while ( i <= j )
    s[k++] = s[i++];
  s[k] = '\0';
  return s;
}
