#ifndef MEMORY_H
#define MEMORY_H

char* _libertymqtt_strdup(const char *str);
void* _libertymqtt_realloc(void *ptr, size_t size);
void* _libertymqtt_malloc(size_t size);
void _libertymqtt_free1(void *p);
#define _libertymqtt_free(ptr) _libertymqtt_free1(ptr);ptr=NULL;

FILE* _libertymqtt_fopen(const char* path, const char* mode);

#endif // MEMORY_H