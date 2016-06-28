#include <time_libertymqtt.h>

time_t libertymqtt_time(){
    struct timespec ts = {0, 0};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec;
}
