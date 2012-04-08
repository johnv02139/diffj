#!/usr/bin/jruby -w
# -*- ruby -*-

require 'rubygems'
require 'riel'
require 'java'
require 'diffj/ast/typeitemdecl'
require 'diffj/ast/type'
require 'diffj/ast/ctor'

include Java

class DiffJ::CtorDeclComparator < DiffJ::TypeItemDeclComparator
  include Loggable

  def initialize diffs
    super diffs, "net.sourceforge.pmd.ast.ASTConstructorDeclaration"
  end

  def get_score from_ctor, to_ctor
    from_ctor.match_score to_ctor
  end

  def do_compare from, to
    ctorcmp = DiffJ::CtorComparator.new filediffs
    ctorcmp.compare_access from.parent, to.parent
    ctorcmp.compare from, to
  end

  def get_name ctordecl
    ctordecl.fullname
  end

  def get_added_message ctordecl
    DiffJ::TypeComparator::CONSTRUCTOR_ADDED
  end

  def get_removed_message ctordecl
    DiffJ::TypeComparator::CONSTRUCTOR_REMOVED
  end
end
