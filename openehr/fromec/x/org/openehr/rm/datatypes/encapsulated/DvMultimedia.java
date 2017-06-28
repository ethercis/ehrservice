/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehr.rm.datatypes.encapsulated;

import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.uri.DvURI;
import org.openehr.rm.support.terminology.OpenEHRCodeSetIdentifiers;
import org.openehr.rm.support.terminology.TerminologyAccess;
import org.openehr.rm.support.terminology.TerminologyService;
import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;

/**
 * A specialisation of Encapsulated for audiovisual and biosignal types.
 * Includes further metadata relating to multimedia types which are not
 * applicable to other subtypes of Encapsulated.
 * Instances of this class are immutable
 *
 * @author Rong Chen
 * @version 1.0
 */
public final class DvMultimedia extends DvEncapsulated {

    /**
     * Constructs a Multimedia by specifying all components
     *
     * @param charset
     * @param language
     * @param size                    >=0
     * @param alternateText
     * @param mediaType               not null and a valid code
     * @param compressionAlgorithm    null or a valid code
     * @param integrityCheck          not null if integrityCheckAlgorithm not null
     * @param integrityCheckAlgorithm null or a valid code
     * @param thumbnail
     * @param uri                   not null if data is null
     * @param data                    not null if uri is null
     * @param terminologyService
     * @throws IllegalArgumentException if any invalid argument
     */
	@FullConstructor
    public DvMultimedia(@Attribute (name = "charset") CodePhrase charset, 
						@Attribute (name = "language") CodePhrase language,
						@Attribute (name = "alternateText") String alternateText,
                        @Attribute (name = "mediaType", required = true) CodePhrase mediaType,
                        @Attribute (name = "compressionAlgorithm") CodePhrase compressionAlgorithm,
                        @Attribute (name = "integrityCheck") byte[] integrityCheck,
                        @Attribute (name = "integrityCheckAlgorithm") CodePhrase integrityCheckAlgorithm,
                        @Attribute (name = "thumbnail") DvMultimedia thumbnail, 
						@Attribute (name = "uri") DvURI uri,
                        @Attribute (name = "data") byte[] data,
                        @Attribute (name = "terminologyService", system = true) TerminologyService terminologyService) {
        
		super(charset, language, terminologyService);
        
		if (mediaType == null) {
            throw new IllegalArgumentException("null mediaType");
        }
//        if (compressionAlgorithm == null) {
//            throw new IllegalArgumentException("null compressionAlgorithm");
//        }
//        if (integrityCheck != null && integrityCheckAlgorithm == null) {
//            throw new IllegalArgumentException(
//                    "null integrity check algorithm");
//        }

//        if (!terminologyService.codeSetForId(
//        		OpenEHRCodeSetIdentifiers.MEDIA_TYPES).hasCode(mediaType)) {
//            throw new IllegalArgumentException(
//                    "unknown media type: " + mediaType);
//        }
        if (compressionAlgorithm != null && !terminologyService.codeSetForId(
        		OpenEHRCodeSetIdentifiers.COMPRESSION_ALGORITHMS).hasCode(
        				compressionAlgorithm)) {
        	throw new IllegalArgumentException("unknown compression algorithm: "
                    + compressionAlgorithm);
        }
        if (integrityCheck != null && !terminologyService.codeSetForId(
        		OpenEHRCodeSetIdentifiers.INTEGRITY_CHECK_ALGORITHMS).hasCode(
        				integrityCheckAlgorithm)) {
        	throw new IllegalArgumentException(
        			"unknown integrity check algorithm: "
        			+ integrityCheckAlgorithm);
        }
        if (uri == null && data == null) {
            throw new IllegalArgumentException("both uri and ata are null");
        }
        this.alternateText = alternateText;
        this.mediaType = mediaType;
        this.compressionAlgorithm = compressionAlgorithm;
        this.integrityCheck = integrityCheck;
        this.integrityCheckAlgorithm = integrityCheckAlgorithm;
        this.thumbnail = thumbnail;
        this.uri = uri;
        this.data = data;
    }
	
	@Override
	public int getSize() {
		return data == null ? 0 : data.length;
	}

    /**
     * Text to display in lieu of multimedia display/replay
     *
     * @return alternate text, null if unspecified
     */
    public String getAlternateText() {
        return alternateText;
    }

    /**
     * Data media type coded from the IANA MIME types,
     * openEHR "media types" code set, see
     * <blockquote>
     * <a href="http://www.iana.org/assignments/mediatypes">
     * <i>http://www.iana.org/assignments/mediatypes</i></a>
     * </blockquote>
     *
     * @return media type
     */
    public CodePhrase getMediaType() {
        return mediaType;
    }

    /**
     * Compression type, a coded value from the openEHR
     * "compression algorithm" code set
     *
     * @return null if no compression
     */
    public CodePhrase getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    /**
     * binary cryptographic integrity checksum
     *
     * @return null if no checksum
     */
    public byte[] getIntegrityCheck() {
        return integrityCheck;
    }

    /**
     * Type of integrity check, a coded value from the openEHR
     * "integrity check algorithm" code set.
     *
     * @return null if has no integrity check
     */
    public CodePhrase getIntegrityCheckAlgorithm() {
        return integrityCheckAlgorithm;
    }

    /**
     * The thumbnail for this item, if one exists; mainly for graphics formats.
     *
     * @return thumbnail null if unspecified
     */
    public DvMultimedia getThumbnail() {
        return thumbnail;
    }

    /**
     * URI reference to electronic information stored outside the record
     *
     * @return possibly null if data inline
     */
    public DvURI getUri() {
        return uri;
    }

    /**
     * The actual data found at 'uri', if supplied inline
     *
     * @return possibly null if data external
     */
    public byte[] getData() {
        return data;
    }

    /**
     * True if the data is stored externally to the record,
     * as indicated by uri
     *
     * @return true if external
     */
    public boolean isExternal() {
        return uri != null;
    }

    /**
     * True if the data is stored in expanded form
     *
     * @return true if data inline
     */
    public boolean isInline() {
        return data != null;
    }

    /**
     * True if the data is stored in compressed form
     *
     * @return true if compressed
     */
    public boolean isCompressed() {
        return compressionAlgorithm != null;
    }

    /**
     * True if an integrity check has been computed
     *
     * @return true if has integrity check
     */
    public boolean hasIntegrityCheck() {
        return integrityCheckAlgorithm != null;
    }

    /**
     * string form displayable for humans
     *
     * @return string presentation
     */
    public String toString() {
        return mediaType.toString();
    }

    // POJO start
    private DvMultimedia() {
    }

    private void setAlternateText(String alternateText) {
        this.alternateText = alternateText;
    }

    private void setMediaType(CodePhrase mediaType) {
        this.mediaType = mediaType;
    }

    private void setCompressionAlgorithm(CodePhrase compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    private void setIntegrityCheck(byte[] integrityCheck) {
        this.integrityCheck = integrityCheck;
    }

    private void setIntegrityCheckAlgorithm(CodePhrase integrityCheckAlgorithm) {
        this.integrityCheckAlgorithm = integrityCheckAlgorithm;
    }

    private void setThumbnail(DvMultimedia thumbnail) {
        this.thumbnail = thumbnail;
    }

    private void setUri(DvURI uri) {
        this.uri = uri;
    }

    private void setData(byte[] data) {
        this.data = data;
    }
    // POJO end

    /* fields */
    private String alternateText;
    private CodePhrase mediaType;
    private CodePhrase compressionAlgorithm;
    private byte[] integrityCheck;
    private CodePhrase integrityCheckAlgorithm;
    private DvMultimedia thumbnail;
    private DvURI uri;
    private byte[] data;	
	@Override
	public String getReferenceModelName() {
		return "DV_MULTIMEDIA";
	}

	@Override
	public String serialise() {
		// TODO Auto-generated method stub
		return null;
	}	
}

