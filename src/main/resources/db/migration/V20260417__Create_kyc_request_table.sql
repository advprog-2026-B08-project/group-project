CREATE TABLE kyc_request (
                             id UUID PRIMARY KEY,

                             user_id UUID,

                             requested_role VARCHAR(255),
                             status VARCHAR(255),

                             email VARCHAR(255),
                             full_name VARCHAR(255),
                             phone_number VARCHAR(50),
                             socials VARCHAR(255),

                             CONSTRAINT fk_kyc_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
);