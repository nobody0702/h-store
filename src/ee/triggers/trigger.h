/* This file is part of VoltDB.
 * Copyright (C) 2008-2010 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
 *
 * VoltDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VoltDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */
/* Copyright (C) 2008 by H-Store Project
 * Brown University
 * Massachusetts Institute of Technology
 * Yale University
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

#ifndef HSTORETRIGGER_H
#define HSTORETRIGGER_H

#include <string>
#include <vector>
#ifdef MEMCHECK_NOFREELIST
#include <set>
#endif
#include "common/ids.h"
#include "common/types.h"
#include "storage/table.h"
#include "catalog/statement.h"
#include "catalog/planfragment.h"
#include "execution/VoltDBEngine.h"
#include "common/executorcontext.hpp"

namespace voltdb {

/**
 * Represents a trigger that is attached to a table.
 */
class Trigger {
    friend class ExecutionEngine;
    friend class ExecutorContext;
    friend class Table;
    friend class PersistantTable;
    friend class TempTable;

  protected:
    catalog::Statement const* m_statement;
    unsigned char m_type; //1=insert, 2=delete, 3=update
    bool m_forEach;
    Table *m_sourceTable;

  public:
    // no default constructor, no copy
    Trigger();
    ~Trigger();

    Trigger(catalog::Statement const*);
    Trigger(catalog::Statement const* stmt, unsigned char type, bool forEach);

    void fire(VoltDBEngine *engine, Table *input);

    void setStatement(catalog::Statement const*);
    bool setType(unsigned char);
    void setForEach(bool);
    void setSourceTable(Table *);

    catalog::Statement const *getStatement();
    unsigned char getType();
    bool getForEach();
    Table *getSourceTable();


};
}
#endif
