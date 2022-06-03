//
// Created by user on 2022-05-25.
//

#include "Acc_lib.h"
#include "string"
Acc_lib::Acc_lib() :accx(0),  accy(0), accz(0){

}

Acc_lib::Acc_lib(int Accx, int Accy, int Accz) :accx(Accx),  accy(Accy), accz(Accz){

}

Acc_lib::~Acc_lib() {

}

std::string
Acc_lib::accString( std::string numx,  std::string numy,  std::string numz) {
    std::string hello = numx+","+numy+","+numz;
    return hello;
}

