/*
 Copyright 2009-2013 Michael Graessle
 
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

#ifndef BREWCTRL_H_
#define BREWCTRL_H_

#define bool _Bool
#define false 0
#define true 1

#define byte  char

#define MODE_OFF 0
#define MODE_ON 1

// Need to remove these
#define LOW 0
#define HIGH 1
#define OUTPUT 0

#ifndef NULL
#define NULL 0
#endif

#ifdef MOCK
#define SYS_PATH "mock/sys"
#endif

#ifndef SYS_PATH
#define SYS_PATH "/sys"
#endif

long millis();
char * generateRandomId();
void initBrewCtrl();
char * mallocStringFromString(char* tmp);

#endif

