from flask import Flask, jsonify, abort, make_response
from flask_restful import Api, Resource, reqparse, marshal
from flasgger import swag_from
from flask_jwt_extended import jwt_required, get_jwt_identity
from utils import email_notifier

import datetime
import uuid

from app import db
from models import models

from resources.fields import issues_fields

class IssueListAPI(Resource):

    def __init__(self):
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument('Dev_id', type=int, default=0,
                                    location='json')
        self.reqparse.add_argument('Cust_id', type=int, default=0,
                                   location='json')
        self.reqparse.add_argument('Prob_id', type=int, default=0,
                                   location='json')
        self.reqparse.add_argument('Delivery_At', type=str, default="Store",
                                   location='json')
        super(IssueListAPI, self).__init__()

    @jwt_required
    @swag_from("apidocs/issues_get.yml")
    def get(self):
        issues = models.Issues.query.all() #Query database
        print(issues)
        return {'Issues': [marshal(issue, issues_fields) for issue in issues]}

    @jwt_required
    @swag_from("apidocs/issues_post.yml")
    def post(self):
        emp_id = get_jwt_identity()
        args = self.reqparse.parse_args()
        print(args)
        issue = models.Issues(
            Dev_id=args["Dev_id"],Cust_id=args["Cust_id"], Prob_id=args["Prob_id"],
            Issue_Created_By=get_jwt_identity(), Issue_Delivery_At=args["Delivery_At"]
            )

        technicians = [emp_role.master_employee for emp_role in models.Emp_Roles.query.filter_by(Role_id=1).all()]
        print("Printing All ", technicians)
        print("Printing First [0] ", technicians[0])
        issue.Issue_Assigned_To = technicians[0].Emp_id
        issue.Issue_Track_Num = uuid.uuid4().hex[:10].upper()

        db.session.add(issue)
        db.session.commit()
        db.session.refresh(issue)

        cust_mail = models.Customers.query.filter_by(Cust_id=args["Cust_id"]).first().Cust_Email
        dev = models.Devices.query.filter_by(Dev_id=args["Dev_id"]).first()
        dev_fmt = "{} {} ({})".format(dev.Dev_Manufacturer, dev.Dev_Model, dev.Dev_Model_Year)
        print("Prepare to send mail to {}".format(cust_mail))

        subject = "MobiRepair Repair Progress"
        content = "You can track the progress of your repair for Device [{}] using the following tracking number [{}]".format(dev_fmt, issue.Issue_Track_Num)

        email_notifier.send_simple_message(cust_mail, content)

        init_timeline = models.Issue_Timeline(Issue_id=issue.Issue_id, Emp_id=emp_id, Act_id=1, Ist_Comment="Generated by system")

        db.session.add(init_timeline)
        db.session.commit()
