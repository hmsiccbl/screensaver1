#! /usr/bin/perl -w
# script to autogenerate an entity


### grobal valuables

my $base_output_dir = "../../src/edu/harvard/med/screensaver/model";
my $odd_plural_to_singular_map = {
    "copies" => "copy",
    "Copies" => "Copy",
};
my $odd_singular_to_plural_map = {
    "copy" => "copies",
    "Copy" => "Copies",
};
my @hashcode_primes = (17, 37, 73, 1771);


### get the entity

my $entity_file = shift or die "usage: $0 entity_file\n";
-r $entity_file or die "entity file $entity_file is unreadable\n";
my $entity = eval `cat $entity_file`;
$entity->{name} or die "malformed entity file: $entity_file";


### extend/convert the entity

add_id_fields($entity);
convert_property_lists_to_hashes($entity);


### open the output file

my $output_filename =
    "$base_output_dir/$entity->{package}/$entity->{name}.java";
print STDERR "writing file $output_filename\n";
open $output_file, "> $output_filename"
    or die "couldnt open file for writing: $output_filename";


### write the output file

print_file_header($output_file, $entity);
print_imports($output_file, $entity);
print_class_header($output_file, $entity);
print_instance_fields($output_file, $entity);
print_constructors($output_file, $entity);
print_public_methods($output_file, $entity);
print_protected_methods($output_file, $entity);
print_package_methods($output_file, $entity);
print_private_methods($output_file, $entity);
print_footer($output_file);
close($output_file);


### subs

sub add_id_fields {
    my ($entity) = @_;
    $entity->{id} = lcfirst($entity->{name}) . "Id";
    $entity->{id_getter} = "get" . $entity->{name} . "Id";
    $entity->{id_setter} = "set" . $entity->{name} . "Id";
    $entity->{lc_underscore_name} = lc_underscore($entity->{name});
    $entity->{id_sequence} = $entity->{lc_underscore_name} . "_id_seq";
}

sub lc_underscore {
    my ($string) = @_;
    my $underscore_name = lcfirst($string);
    $underscore_name =~ s/([A-Z])/_$1/g;
    $underscore_name = lc($underscore_name);
}

sub convert_property_lists_to_hashes {
    my ($entity) = @_;
    my $properties = $entity->{properties};
    for (my $i = 0; $i < @$properties; $i++) {
        my $property = $properties->[$i];
        my ($name,
            $human_name,
            $type,
            $is_vocab,
            $is_required,
            $is_unique,
            $has_many,
            $is_relationship,
            $package,
            $other_side_has_many,
            $other_side_is_required,
            $is_inverse) = @$property;

        $is_required && $has_many &&
            die "property cannot be required and has-many: $property->{name}";
        $is_unique && ! $is_required &&
            die "property cannot be unique and not required: $property->{name}";

        my $singular_name = undef;
        if ($has_many) {
            $singular_name = ($odd_plural_to_singular_map->{$name}) ?
                $odd_plural_to_singular_map->{$name} :
                substr($name, 0, length($name) - 1);
        }
        my $hbn_name =
            $is_relationship ? "hbn" . ucfirst($name) : undef;
        $properties->[$i] = {
            name                   => $name,
            human_name             => $human_name,
            singular_name          => $singular_name,
            hbn_name               => $hbn_name,
            type                   => $type,
            is_vocab               => $is_vocab,
            is_required            => $is_required,
            is_unique              => $is_unique,
            has_many               => $has_many,
            is_relationship        => $is_relationship,
            package                => $package,
            other_side_has_many    => $other_side_has_many,
            other_side_is_required => $other_side_is_required,
            is_inverse             => $is_inverse,
        };
    }
}

sub print_file_header {
    my ($output_file, $entity) = @_;
    print $output_file <<END_FILE_HEADER;
// \$HeadURL\$
// \$Id\$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.$entity->{package};
END_FILE_HEADER
}

sub print_imports {
    my ($output_file, $entity) = @_;
    print $output_file "\n";
    if (has_relation_has_many($entity)) {
        print $output_file "import java.util.Collections;\n";
    }
    if (has_date($entity)) {
        print $output_file "import java.util.Date;\n";
    }
    if (has_has_many($entity)) {
        print $output_file "import java.util.HashSet;\n";
        print $output_file "import java.util.Set;\n";
    }
    print $output_file "\n";
    print $output_file "import org.apache.log4j.Logger;\n\n";
    print $output_file "import edu.harvard.med.screensaver.model.AbstractEntity;\n";

    my @entity_imports = get_entity_imports($entity);
    for my $entity_import (@entity_imports) {
        print $output_file
            "import edu.harvard.med.screensaver.model.", $entity_import, ";\n";
    }
}

sub get_entity_imports {
    my ($entity) = @_;
    my @entity_imports = ();
    for my $property (@{ $entity->{properties} }) {
        if ($property->{is_relationship} &&
            $property->{package}) {
            push @entity_imports, $property->{package} . $property->{type};
        }
    }
    return @entity_imports;
}

sub print_class_header {
    my ($output_file, $entity) = @_;
    print $output_file <<END_CLASS_HEADER;


/**
 * A Hibernate entity bean representing a $entity->{human_name}.
 * 
 * \@author <a mailto=\"john_sullivan\@hms.harvard.edu\">John Sullivan</a>
 * \@author <a mailto=\"andrew_tolopko\@hms.harvard.edu\">Andrew Tolopko</a>
 * \@hibernate.class lazy="false"
 */
public class $entity->{name} extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger($entity->{name}.class);
  private static final long serialVersionUID = 0L;
END_CLASS_HEADER
}

sub print_instance_fields {
    my ($output_file, $entity) = @_;
    print $output_file "\n\n  // instance fields\n\n";
    print $output_file "  private Integer _$entity->{id};\n";
    print $output_file "  private Integer _version;\n";

    for my $property (@{ $entity->{properties} }) {
        print $output_file "  private ";
        if ($property->{has_many}) {
            print $output_file "Set<$property->{type}>";
        }
        else {
            print $output_file "$property->{type}";
        }
        print $output_file " _$property->{name}";
        if ($property->{has_many}) {
            print $output_file " = new HashSet<$property->{type}>()";
        }
        print $output_file ";\n";
    }
}

sub print_constructors {
    my ($output_file, $entity) = @_;
    my @required_properties = grep {
        $_->{is_required}
    } @{ $entity->{properties} };
    print $output_file
        "\n\n  // public constructor\n\n",
        "  /**\n",
        "   * Constructs an initialized <code>$entity->{name}</code> object.\n",
        "   *\n";
    for my $property (@required_properties) {
        print $output_file "   * \@param $property->{name} the $property->{human_name}\n";
    }
    print $output_file
        "   */\n",
        "  public $entity->{name}(\n";
    for (my $i = 0; $i < @required_properties; $i++) {
        my $property = $required_properties[$i];
        print $output_file "    $property->{type} $property->{name}";
        if ($i != $#required_properties) {
            print $output_file ",\n";
        }
    }
    print $output_file ")\n  {\n";
    print $output_file "    // TODO: verify the order of assignments here is okay\n";
    for my $property (@required_properties) {
        print $output_file "    _$property->{name} = $property->{name};\n";
    }
    print $output_file "  }\n";
}

sub print_public_methods {
    my ($output_file, $entity) = @_;
    print $output_file "\n\n  // public methods\n\n";

    # getEntityId
    print $output_file
        "  \@Override\n",
        "  public Integer getEntityId()\n",
        "  {\n",
        "    return $entity->{id_getter}();\n",
        "  }\n\n";

    # id getter
    print $output_file
        "  /**\n",
        "   * Get the id for the $entity->{human_name}.\n",
        "   *\n",
        "   * \@return the id for the $entity->{human_name}\n",
        "   * \@hibernate.id generator-class=\"sequence\"\n",
        "   * \@hibernate.generator-param name=\"sequence\" value=\"$entity->{id_sequence}\"\n",
        "   */\n",
        "  public Integer $entity->{id_getter}()\n",
        "  {\n",
        "    return _$entity->{id};\n",
        "  }\n";

    for my $property (@{ $entity->{properties} }) {
        print_public_methods_for_property($output_file, $entity, $property);
    }    
}

sub print_public_methods_for_property {
    my ($output_file, $entity, $property) = @_;
    print_getter_for_property($output_file, $entity, $property, "public");
    unless ($property->{has_many}) {
        print_setter_for_property($output_file, $entity, $property, "public");
    }
    else {
        print_adder_and_remover_for_property($output_file, $entity, $property);
    }
}

sub print_getter_for_property {
    my ($output_file, $entity, $property, $access, $skip_hibernate_xdoclet) = @_;
    my $singular_name = lc_underscore($property->{singular_name});

    print $output_file "\n";
    if ($access eq "public" && $property->{is_relationship} &&
        $property->{has_many}) {
        print $output_file
            "  /**\n",
            "   * Get an unmodifiable copy of the set of $property->{human_name}.\n",
            "   *\n",
            "   * \@return the $property->{human_name}\n";
    }
    else {
        print $output_file
            "  /**\n",
            "   * Get the $property->{human_name}.\n",
            "   *\n",
            "   * \@return the $property->{human_name}\n";
    }

    if (! $property->{is_relationship} && ! $skip_hibernate_xdoclet) {
        if ($property->{has_many}) {
            my $entity_lc_underscore_name = $entity->{lc_underscore_name};
            if ($property->{other_side_has_many}) {
                $entity_lc_underscore_name =
                    substr($entity_lc_underscore_name, 0, length($entity_lc_underscore_name) - 1);
            }
            my $table_name = "${entity_lc_underscore_name}_$singular_name";
            print $output_file
                "   * \@hibernate.set\n",
                "   *   order-by=\"$singular_name\"\n",
                "   *   table=\"$table_name\"\n",
                "   *   cascade=\"delete\"\n",
                "   *   lazy=\"true\"\n",
                "   * \@hibernate.collection-key\n",
                "   *   column=\"$entity->{lc_underscore_name}_id\"\n",
                "   *   foreign-key=\"fk_${table_name}_to_$entity->{lc_underscore_name}\"\n",
                "   * \@hibernate.collection-element\n";
            if ($property->{type} eq "String") {
                print $output_file "   *   type=\"text\"\n";
            }
            elsif ($property->{type} eq "Integer") {
                print $output_file "   *   type=\"int\"\n";
            }
            print $output_file
                "   *   column=\"$singular_name\"\n",
                "   *   not-null=\"true\"\n";
        }
        else {
            print $output_file "   * \@hibernate.property\n";
            if ($property->{is_vocab}) {
                print $output_file "   *   type=\"edu.harvard.med.screensaver.model.$entity->{package}.$property->{type}\$UserType\"\n";
            }
            elsif ($property->{type} eq "String") {
                print $output_file "   *   type=\"text\"\n";
            }
            if ($property->{is_required}) {
                print $output_file "   *   not-null=\"true\"\n";
            }
            if ($property->{is_unique}) {
                print $output_file "   *   unique=\"true\"\n";
            }
        }
    }

    print $output_file "   */\n  $access ";
    if ($property->{has_many}) {
        print $output_file "Set<$property->{type}>";
    }
    else {
        print $output_file $property->{type};
    }

    print $output_file " get", ucfirst($property->{name}), "()\n  {\n";
    if ($property->{has_many} && $property->{is_relationship}) {
        print $output_file "    return Collections.unmodifiableSet(_$property->{name});\n";
    }
    else {
        print $output_file "    return _$property->{name};\n";
    }
    print $output_file "  }\n";
}

sub print_setter_for_property {
    my ($output_file, $entity, $property, $access) = @_;

    print $output_file "\n";
    print $output_file
        "  /**\n",
        "   * Set the $property->{human_name}.\n",
        "   *\n",
        "   * \@param $property->{name} the new $property->{human_name}\n";
    if ($property->{has_many}) {
        print $output_file "   * \@motivation for hibernate\n";
    }
    print $output_file
        "   */\n",
        "  $access void set", ucfirst($property->{name}), "(";
    if ($property->{has_many}) {
        print $output_file "Set<$property->{type}> $property->{name})\n",
    }
    else {
        print $output_file "$property->{type} $property->{name})\n",
    }
    print $output_file "  {\n";
    print $output_file "    _$property->{name} = $property->{name};\n";
    if ($property->{is_relationship}) {
        if ($property->{other_side_has_many}) {
            my $plural_name =
                $odd_singular_to_plural_map->{$entity->{name}} ?
                $odd_singular_to_plural_map->{$entity->{name}} :
                $entity->{name} . "s";
            print $output_file
                "    $property->{name}.getHbn",
                ucfirst($plural_name),
                "().add(this);\n";
        }
        else {
            print $output_file
                "    $property->{name}.setHbn",
                ucfirst($entity->{name}),
                "(this);\n";
        }
    }
    print $output_file "  }\n";
}

sub print_adder_and_remover_for_property {
    my ($output_file, $entity, $property) = @_;
    my $singular_human_name = $property->{human_name};
    $singular_human_name =
        substr($singular_human_name, 0, length($singular_human_name) - 1);

    print $output_file "\n";
    print $output_file
        "  /**\n",
        "   * Add the $singular_human_name.\n",
        "   *\n",
        "   * \@param $property->{singular_name} the $singular_human_name to add\n",
        "   * \@return true iff the $entity->{human_name} did not already have the $singular_human_name\n",
        "   */\n",
        "  public boolean add", ucfirst($property->{singular_name}), "($property->{type} $property->{singular_name})\n",
        "  {\n";
    if ($property->{is_relationship}) {
        print $output_file
            "    if (get", ucfirst($property->{hbn_name}), "().add($property->{singular_name})) {\n";
        if ($property->{other_side_has_many}) {
            my $plural_name =
                $odd_singular_to_plural_map->{$entity->{name}} ?
                $odd_singular_to_plural_map->{$entity->{name}} :
                $entity->{name} . "s";
            print $output_file
                "      return $property->{singular_name}.getHbn", ucfirst($plural_name), "().add(this);\n";
        }
        else {
            print $output_file
                "      $property->{singular_name}.setHbn", ucfirst($entity->{name}), "(this);\n",
                "      return true;\n";
        }
        print $output_file
            "    }\n",
            "    return false;\n";
    }
    else {
        print $output_file
            "    return _$property->{name}.add($property->{singular_name});\n";
    }
    print $output_file "  }\n";

    return if $property->{other_side_is_required};


    print $output_file "\n";
    print $output_file
        "  /**\n",
        "   * Remove the $singular_human_name.\n",
        "   *\n",
        "   * \@param $property->{singular_name} the $singular_human_name to remove\n",
        "   * \@return true iff the $entity->{human_name} previously had the $singular_human_name\n",
        "   */\n",
        "  public boolean remove", ucfirst($property->{singular_name}), "($property->{type} $property->{singular_name})\n",
        "  {\n";
    if ($property->{is_relationship}) {
        print $output_file
            "    if (get", ucfirst($property->{hbn_name}), "().remove($property->{singular_name})) {\n";
        if ($property->{other_side_has_many}) {
            my $plural_name =
                $odd_singular_to_plural_map->{$entity->{name}} ?
                $odd_singular_to_plural_map->{$entity->{name}} :
                $entity->{name} . "s";
            print $output_file
                "      return $property->{singular_name}.getHbn", ucfirst($plural_name), "().remove(this);\n";
        }
        else {
            print $output_file
                "      return $property->{singular_name}.setHbn", ucfirst($entity->{name}), "(null);\n",
                "      return true;\n";
        }
        print $output_file
            "    }\n",
            "    return false;\n";
    }
    else {
        print $output_file
            "    return _$property->{name}.remove($property->{singular_name});\n";
    }
    print $output_file "  }\n";
}

sub print_protected_methods {
    my ($output_file, $entity) = @_;
    print $output_file "\n\n  // protected methods\n\n";

    if (ref($entity->{business_key}) eq "ARRAY") {
        my @key_properties = @{ $entity->{business_key} };

        print $output_file
            "  /**\n",
            "   * A business key class for the well.\n",
            "   */\n",
            "  private class BusinessKey\n",
            "  {\n";

        for my $key_property (@key_properties) {
            for my $property (@{ $entity->{properties} }) {
                next unless $key_property eq $property->{name};
                print_getter_for_property($output_file, $entity, $property, "public", 1);
                last;
            }
        }

        print $output_file "\n";
        print $output_file
            "    \@Override\n",
            "    public boolean equals(Object object)\n",
            "    {\n",
            "      if (! (object instanceof BusinessKey)) {\n",
            "        return false;\n",
            "      }\n",
            "      BusinessKey that = (BusinessKey) object;\n",
            "      return\n";
        for my $key_property (@key_properties) {
            my $getter = "get" . ucfirst($key_property) . "()";
            print $output_file "        $getter.equals(that.$getter)";
            if ($key_property eq $key_properties[-1]) {
                print $output_file ";\n";
            }
            else {
                print $output_file " &&\n";
            }
        }
        print $output_file "    }\n\n";

        print $output_file
            "    \@Override\n",
            "    public int hashCode()\n",
            "    {\n",
            "      return\n";
        for my $key_property (@key_properties) {
            my $getter = "get" . ucfirst($key_property) . "()";
            print $output_file "        $getter.hashCode()";
            if ($key_property eq $key_properties[-1]) {
                print $output_file ";\n";
            }
            else {
                print $output_file " +\n";
            }
        }
        print $output_file
            "    }\n\n";

        print $output_file
            "    \@Override\n",
            "    public String toString()\n",
            "    {\n",
            "      return ";
        for my $key_property (@key_properties) {
            my $getter = "get" . ucfirst($key_property) . "()";
            print $output_file $getter;
            if ($key_property eq $key_properties[-1]) {
                print $output_file ";\n";
            }
            else {
                print $output_file " + \":\" + ";
            }
        }
        print $output_file
            "    }\n",
            "  }\n\n";
    }

    print $output_file
        "  \@Override\n",
        "  protected Object getBusinessKey()\n",
        "  {\n",
        "    // TODO: assure changes to business key update relationships whose other side is many\n";
    if (ref($entity->{business_key}) ne "ARRAY") {
        print $output_file
            "    return get", ucfirst($entity->{business_key}), "();\n";
    }
    else {
        print $output_file
            "    return new BusinessKey();\n";
    }
    print $output_file "  }\n";
}

sub print_package_methods {
    my ($output_file, $entity) = @_;
    print $output_file "\n\n  // package methods\n";

    for my $property (@{ $entity->{properties} }) {
        next unless $property->{is_relationship};
        if ($property->{has_many}) {
            print_hbn_getter_for_property($output_file, $entity, $property, "package");
        }
        else {
            print_hbn_setter_for_property($output_file, $entity, $property, "package");
        }
    }
}


sub print_hbn_setter_for_property {
    my ($output_file, $entity, $property, $access) = @_;

    print $output_file "\n";
    print $output_file
        "  /**\n",
        "   * Set the $property->{human_name}.\n";
    if ($property->{is_required}) {
        print $output_file "   * Throw a NullPointerException when the $property->{human_name} is null.\n";
    }
    print $output_file
        "   *\n",
        "   * \@param $property->{name} the new $property->{human_name}\n";
    if ($property->{is_required}) {
        print $output_file "   * \@throws NullPointerException when the $property->{human_name} is null\n";
    }
    print $output_file "   * \@motivation for hibernate";
    if ($access ne "private") {
        print $output_file " and maintenance of bi-directional relationships";
    }
    print $output_file "\n   */\n  ";
    if ($access ne "package") {
        print $output_file "$access ";
    }
    print $output_file "void setHbn", ucfirst($property->{name}), "(";
    if ($property->{has_many}) {
        print $output_file "Set<$property->{type}>";
    }
    else {
        print $output_file $property->{type};
    }
    print $output_file " $property->{name})\n  {\n";
    if ($property->{is_required}) {
        print $output_file
            "    if ($property->{name} == null) {\n",
            "      throw new NullPointerException();\n",
            "    }\n";
    }
    print $output_file
        "    _$property->{name} = $property->{name};\n",
        "  }\n";
}

sub print_private_methods {
    my ($output_file, $entity) = @_;
    print $output_file "\n\n  // private constructor\n\n";

    print $output_file
        "  /**\n",
        "   * Construct an uninitialized <code>$entity->{name}</code> object.\n",
        "   *\n",
        "   * \@motivation for hibernate\n",
        "   */\n",
        "  private $entity->{name}() {}\n";

    print $output_file "\n\n  // private methods\n\n";

    print $output_file
        "  /**\n",
        "   * Set the id for the $entity->{human_name}.\n",
        "   *\n",
        "   * \@param $entity->{id} the new id for the $entity->{human_name}\n",
        "   * \@motivation for hibernate\n",
        "   */\n",
        "  private void $entity->{id_setter}(Integer $entity->{id}) {\n",
        "    _$entity->{id} = $entity->{id};\n",
        "  }\n\n";

    print $output_file
        "  /**\n",
        "   * Get the version for the $entity->{human_name}.\n",
        "   *\n",
        "   * \@return the version for the $entity->{human_name}\n",
        "   * \@motivation for hibernate\n",
        "   * \@hibernate.version\n",
        "   */\n",
        "  private Integer getVersion() {\n",
        "    return _version;\n",
        "  }\n\n";

    print $output_file
        "  /**\n",
        "   * Set the version for the $entity->{human_name}.\n",
        "   *\n",
        "   * \@param version the new version for the $entity->{human_name}\n",
        "   * \@motivation for hibernate\n",
        "   */\n",
        "  private void setVersion(Integer version) {\n",
        "    _version = version;\n",
        "  }\n";

    for my $property (@{ $entity->{properties} }) {
        if (! $property->{has_many} && $property->{is_relationship}) {
            print_hbn_getter_for_property($output_file, $entity, $property, "private");
        }
        if ($property->{has_many}) {
            if ($property->{is_relationship}) {
                print_hbn_setter_for_property($output_file, $entity, $property, "private");
            }
            else {
                print_setter_for_property($output_file, $entity, $property, "private");
            }
        }
    }
}

sub print_hbn_getter_for_property {
    my ($output_file, $entity, $property, $access) = @_;

    print $output_file "\n";
    print $output_file
        "  /**\n",
        "   * Get the $property->{human_name}.\n",
        "   *\n",
        "   * \@return the $property->{human_name}\n";

    my $lc_underscore_name = lc_underscore($property->{name});
    my $uc_property_name = ucfirst($property->{name});

    if ($property->{has_many}) {
        $uc_property_name =
            $odd_plural_to_singular_map->{$uc_property_name} ?
            $odd_plural_to_singular_map->{$uc_property_name} :
            substr($uc_property_name, 0, length($uc_property_name) - 1);
        if ($property->{other_side_has_many}) {
            my $singular_name = substr($lc_underscore_name, 0, length($lc_underscore_name) - 1);
            my $link_table =
                ($property->{is_inverse}) ?
                "${singular_name}_$entity->{lc_underscore_name}_link" :
                "$entity->{lc_underscore_name}_${singular_name}_link";

            print $output_file "   * \@hibernate.set\n";
            if ($property->{is_inverse}) {
                print $output_file "   *   inverse=\"true\"\n";
            }
            print $output_file
                "   *   table=\"$link_table\"\n",
                "   *   cascade=\"all\"\n",
                "   * \@hibernate.collection-key\n",
                "   *   column=\"$entity->{lc_underscore_name}_id\"\n",
                "   * \@hibernate.collection-many-to-many\n",
                "   *   column=\"${singular_name}_id\"\n",
                "   *   class=\"edu.harvard.med.screensaver.model.$entity->{package}.$uc_property_name\"\n",
                "   *   foreign-key=\"fk_${link_table}_to_$entity->{lc_underscore_name}\"\n";
        }
        else {
            print $output_file
                "   * \@hibernate.set\n",
                "   *   cascade=\"save-update\"\n",
                "   *   inverse=\"true\"\n",
                "   * \@hibernate.collection-key\n",
                "   *   column=\"$entity->{lc_underscore_name}_id\"\n",
                "   * \@hibernate.collection-one-to-many\n",
                "   *   class=\"edu.harvard.med.screensaver.model.$entity->{package}.$uc_property_name\"\n";
        }
    }
    else {
        print $output_file
            "   * \@hibernate.many-to-one\n",
            "   *   class=\"edu.harvard.med.screensaver.model.$entity->{package}.$uc_property_name\"\n",
            "   *   column=\"${lc_underscore_name}_id\"\n";
        if ($property->{is_required}) {
            print $output_file
                "   *   not-null=\"true\"\n";
        }
        print $output_file
            "   *   foreign-key=\"fk_$entity->{lc_underscore_name}_to_$lc_underscore_name\"\n",
            "   *   cascade=\"save-update\"\n";
    }

    print $output_file "   * \@motivation for hibernate";
    if ($access ne "private") {
        print $output_file " and maintenance of bi-directional relationships";
    }
    print $output_file "\n   */\n  ";
    if ($access ne "package") {
        print $output_file "$access ";
    }
    if ($property->{has_many}) {
        print $output_file "Set<$property->{type}>";
    }
    else {
        print $output_file $property->{type};
    }

    print $output_file
        " getHbn", ucfirst($property->{name}), "()\n",
        "  {\n",
        "    return _$property->{name};\n",
        "  }\n";
}


sub print_footer {
    my ($output_file) = @_;
    print $output_file <<END_FILE_FOOTER
}
END_FILE_FOOTER
}

sub has_has_many {
    my ($entity) = @_;
    my $properties = $entity->{properties};
    for my $property (@$properties) {
        return 1 if $property->{has_many};
    }
    return 0;
}

sub has_date {
    my ($entity) = @_;
    my $properties = $entity->{properties};
    for my $property (@$properties) {
        return 1 if $property->{type} eq "Date";
    }
    return 0;
}

sub has_relation_has_many {
    my ($entity) = @_;
    my $properties = $entity->{properties};
    for my $property (@$properties) {
        return 1 if $property->{has_many} && $property->{is_relationship};
    }
    return 0;
}
