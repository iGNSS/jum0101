//
// Created by user on 2022-05-25.
//

#ifndef C_C_ACC_LIB_H
#define C_C_ACC_LIB_H
#include "string"

class Acc_lib {
private:
    int accx;
    int accy;
    int accz;
public:
    Acc_lib();
    Acc_lib(int accx,int accy,int accz);
    std::string accString(const std::string numx,const std::string numy,const std::string numz) ;
    ~Acc_lib();
};


#endif //C_C_ACC_LIB_H
