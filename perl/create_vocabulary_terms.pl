#! /usr/bin/perl -w
# script to autogenerate the VocaularyTerms


### grobal constants

my $base_output_dir = "../src/edu/harvard/med/screensaver/model";


### data structures describing the vocabularies to generate

my $users_vocabularies = {
    UserClassification => {
        human_name => "user classification",
        terms => ["Principal Investigator",
                  "Graduate Student",
                  "ICCB Fellow",
                  "Research Assistant",
                  "Postdoc",
                  "ICCB-L/NSRB staff",
                  "Other",
                  ],
    },
    AffiliationCategory => {
        human_name => "affiliation category",
        terms => ["HMS",
                  "HMS Affiliated Hospital",
                  "HSPH",
                  "Broad/ICG",
                  "Harvard FAS",
                  "Other",
                  ],
    },
};

my $screens_vocabularies = {
    ScreenType => {
        human_name => "screen type",
        terms => ["Small Molecule",
                  "RNAi",
                  ],
    },
    FundingSupport => {
        human_name => "funding support",
        terms => ["Clardy Grants",
                  "D'Andrea CMCR",
                  "ICCB-L HMS Internal",
                  "ICCB-L External",
                  "Mitchison P01",
                  "ICG",
                  "NERCE/NSRB",
                  "Sanofi-Aventis",
                  "Yuan NIH 06-07",
                  "Other",
                  ],
    },
    AssayReadoutType => {
        human_name => "assay readout type",
        terms => ["Photometry",
                  "Luminescence",
                  "Fluorescence Intensity",
                  "FP",
                  "FRET",
                  "Imaging",
                  "Unspecified",
                  ],
    },
    StatusValue => {
        human_name => "status_value",
        terms => ["Accepted",
                  "Completed",
                  "Completed - Duplicate with Ongoing",
                  "Dropped - Technical",
                  "Dropped - Resources",
                  "Never Initiated",
                  "Ongoing",
                  "Pending",
                  "Transferred to Broad Institute",
                  ],
    },
    IsFeeToBeChargedForScreening => {
        human_name => "is fee to be charged for screening",
        terms => ["Yes",
                  "No",
                  "No, see comments",
                  ],
    },
    VisitType => {
        human_name => "visit type",
        terms => ["Library",
                  "Cherry Pick",
                  "Liquid Handling Only",
                  "Special",
                  ],
    },
    AssayProtocolType => {
        human_name => "assay protocol type",
        terms => ["Preliminary",
                  "Established",
                  "Protocol last modified on",
                  ],
    },
    MethodOfQuantification => {
        human_name => "method of quantification",
        terms => ["RTPCR",
                  "Branched DNA",
                  "Western",
                  "Northern",
                  "IP Western",
                  "Immunoflourescence",
                  "Other",
                  ],
    },
};

##**** left off checking

my $screenresults_vocabularies = {
    ActivityIndicatorType => {
        human_name => "activity indicator type",
        terms => ["Numerical",
                  "Boolean",
                  "Partition",
                  ],
    },
    IndicatorDirection => {
        human_name => "indicator direction",
        terms => ["High Values Indicate",
                  "Low Values Indicate",
                  ],
    },
};

my $libraries_vocabularies = {
    LibraryType => {
        human_name => "library type",
        terms => ["Commercial",
                  "DOS",
                  "Annotation",
                  "Discrete",
                  "Known Bioactives",
                  "NCI",
                  "Natural Products",
                  "RNAi",
                  "Other",
                  ],
    },                      
    IsScreenable => {
        human_name => "is screenable",
        terms => ["Yes",
                  "No",
                  "Not Recommended",
                  "Not Yet Plated",
                  "Retired",
                  ],
    },
    PlateType => {
        human_name => "plate type",
        terms => ["Marsh",
               "ABgene",
               "Genetix",
               ],
    },
    SilencingReagentType => {
        human_name => "silencing reagent type",
        terms => ["siRNA",
                  "shRNA",
                  "dsRNA",
                  "esiRNA"],
    },
};

my $packages = {
    users => $users_vocabularies,
    screens => $screens_vocabularies,
    screenresults => $screenresults_vocabularies,
    libraries => $libraries_vocabularies,
};


### the code to generate the vocabularies

for my $package_name (keys %$packages) {
    my $package = $packages->{$package_name};
    print STDERR "generating package $package_name..\n";

    for my $vocabulary_name (keys %$package) {
        my $vocabulary_human_name = $package->{$vocabulary_name}->{human_name};
        my @vocabulary_terms = @{ $package->{$vocabulary_name}->{terms} };
        print STDERR "generate vocabulary $vocabulary_name..\n";

        my $output_filename =
            "$base_output_dir/$package_name/$vocabulary_name.java";
        print STDERR "writing file $output_filename\n";
        open $output_file, "> $output_filename"
            or die "couldnt open file for writing: $output_filename";

        print $output_file <<FILE_HEADER; #** Java follows
// \$HeadURL\$
// \$Id\$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.$package_name;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The $vocabulary_human_name vocabulary.
 * 
 * \@author <a mailto=\"john_sullivan\@hms.harvard.edu\">John Sullivan</a>
 * \@author <a mailto=\"andrew_tolopko\@hms.harvard.edu\">Andrew Tolopko</a>
 */
public enum $vocabulary_name implements VocabularyTerm
{

  // the vocabulary
  
FILE_HEADER

#** back in the Perl world here..

        for my $vocabulary_term (@vocabulary_terms) {
            my $enum_value = uc($vocabulary_term);
            $enum_value =~ s/ICCB-L/ICCBL/g;
            $enum_value =~ s/[ ,\'-\/]/_/g;

            my $last_term =
                $vocabulary_term eq $vocabulary_terms[$#vocabulary_terms];

            print $output_file "  $enum_value(\"$vocabulary_term\")";
            print $output_file "," unless $last_term;
            print $output_file "\n";

            print STDERR "got term $vocabulary_term\n";
        }

        print $output_file <<FILE_FOOTER; #** Java follows..
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {\@link $vocabulary_name} vocabulary.
   */
  public static class UserType extends VocabularyUserType<$vocabulary_name>
  {
    public UserType()
    {
      super($vocabulary_name.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>$vocabulary_name</code> vocabulary term.
   * \@param value The value of the term.
   */
  private $vocabulary_name(String value)
  {
    _value = value;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * \@return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  /* (non-Javadoc)
   * \@see java.lang.Object#toString()
   */
  \@Override
  public String toString()
  {
    return getValue();
  }
}
FILE_FOOTER

#** back in the Perl world..

        close $output_file;
    }
}
