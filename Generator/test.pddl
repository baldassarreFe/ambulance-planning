(define (problem test)
(:domain ambulance world)
(:objects l0 l1 l2 l3 l4 l5 l6 l7 l8 l9 p0 p1 p2 p3 p4 a0 a1 a2 h0 h1 )
(:init (Location(l0,0,8,0))
(Location(l1,0,1,45))
(Road(l0,l1,7.0901498479719445))
(Location(l2,3,5,11))
(Road(l1,l2,5.097953054477776))
(Location(l3,6,9,30))
(Road(l2,l3,5.200572042395832))
(Location(l4,5,9,41))
(Road(l1,l4,9.587886785436515))
(Location(l5,3,1,4))
(Road(l1,l5,3.419484347678428))
(Location(l6,0,3,45))
(Road(l0,l6,5.502326251277954))
(Location(l7,6,8,12))
(Road(l4,l7,2.390860355278866))
(Location(l8,7,2,29))
(Road(l6,l8,7.404242078969543))
(Location(l9,6,2,14))
(Road(l3,l9,7.2106141239154775))
(Patient(p0,3))
(Patient(p1,3))
(Patient(p2,3))
(Patient(p3,3))
(Patient(p4,3))
(Ambulance(a0))
(Ambulance(a1))
(Ambulance(a2))
(Hospital(h0))
(Hospital(h1))
(At(p0,l1))
(At(p1,l6))
(At(p2,l4))
(At(p3,l3))
(At(p4,l8))
(Waiting(p0))
(Waiting(p1))
(Waiting(p2))
(Waiting(p3))
(Waiting(p4))
(At(a0,l0))
(Available(a0))
(At(a1,l9))
(Available(a1))
(At(a2,l9))
(Available(a2))
(At(h0,l2))
(At(h1,l5))
)
(:goal (InHospital(p0))
(InHospital(p1))
(InHospital(p2))
(InHospital(p3))
(InHospital(p4))
))