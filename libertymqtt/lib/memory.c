#include <config.h>

char* _libertymqtt_strdup(const char *str){
    char *p = strdup(str);
#ifdef REAL_WITH_MEMORY_TRACKING
    memcount += malloc_usable_size(p);
    if (memcount > max_memcount)
        max_memcount = memcount;
#endif
    return p;
}
