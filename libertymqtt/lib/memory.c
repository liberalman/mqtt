/**************************************************************************/
/** 
* @brief 内存操作封装
* @details 这里如果定义MEMORY_STATISTICS宏，就会来统计内存使用量，如果不定义则不统计。
* @addtogroup memory 内存
* @author 首超
* @version 0.01 
* @date 2015/05/18 
****************************************************************************** 
* Copyright (c), 2015, Liberty Co., Ltd. 
****************************************************************************** 
* Edit History /n 
* -------------------------------------------------------------------------/n 
* DATE NAME DESCRIPTION /n 
* 2015/05/19 首超 Create./n 
****************************************************************************** 
* @{ 
*****************************************************************************/
#include <config.h>

#ifdef MEMORY_STATISTICS
#include <execinfo.h>

#include <malloc.h>
/** 内存分配数 */
static uint64_t memcount = 0;
/** 已分配的最大内存数 */
static uint64_t max_memcount = 0;

/** Obtain a backtrace and print it to @code{stdout}. */
void print_trace (void)
{
    void *array[10];
    size_t size;
    char **strings;
    size_t i;

    size = backtrace (array, 10);
    strings = backtrace_symbols (array, size);
    if (NULL == strings)
    {
        perror("backtrace_synbols");
        exit(-1);
    }

    printf ("Obtained %zd stack frames.\n", size);

    for (i = 0; i < size; i++)
        printf ("%s\n", strings[i]);

    free (strings);
    strings = NULL;
}
#endif

char* _libertymqtt_strdup(const char *str){
    char *p = strdup(str);
#ifdef MEMORY_STATISTICS
    memcount += malloc_usable_size(p); // 统计内存分配数
    if (memcount > max_memcount)
        max_memcount = memcount; // 更新已分配的最大内存数
#endif
    return p;
}

void* _libertymqtt_malloc(size_t size){
    void *ptr = malloc(size);
#ifdef MEMORY_STATISTICS
    memcount += malloc_usable_size(ptr); // 统计内存分配数
    if (memcount > max_memcount)
        max_memcount = memcount; // 更新已分配的最大内存数
    //print_trace();
#endif
    return ptr;
}

void* _libertymqtt_realloc(void *ptr, size_t size){
#ifdef MEMORY_STATISTICS
    if (ptr)
        memcount -= malloc_usable_size(ptr); // 更新已分配的最大内存数
#endif
    void *newptr = realloc(ptr, size);
#ifdef MEMORY_STATISTICS
    memcount += malloc_usable_size(ptr); // 统计内存分配数
    if (memcount > max_memcount)
        max_memcount = memcount; // 更新已分配的最大内存数
#endif
    return newptr;
}

void _libertymqtt_free1(void *p) {
#ifdef MEMORY_STATISTICS
    memcount -= malloc_usable_size(p);
#endif
    if(NULL != p)
        free(p);
}

FILE* _libertymqtt_fopen(const char* path, const char* mode){
    _log(INFO, "创建文件%s\n", path);
    return fopen(path, mode);
}

/** @}***********************************************************/ 
